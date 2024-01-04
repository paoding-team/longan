package dev.paoding.longan.core;

import dev.paoding.longan.annotation.Mapping;
import dev.paoding.longan.annotation.RpcService;
import dev.paoding.longan.channel.Channel;
import dev.paoding.longan.channel.http.*;
import dev.paoding.longan.doc.DocumentService;
import dev.paoding.longan.channel.dubbo.DubboInterceptor;
import dev.paoding.longan.channel.dubbo.DubboFilter;
import dev.paoding.longan.data.Entity;
import dev.paoding.longan.channel.http.WebSocketListener;
import dev.paoding.longan.util.StringUtils;
import org.apache.dubbo.config.ServiceConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Component
public class ServiceInitializingBean implements BeanFactoryAware, InitializingBean {
    @Resource
    private HttpServiceInvoker httpServiceInvoker;
    @Resource
    private ApplicationContext context;
    private DubboInterceptor dubboInterceptor;
    @Resource
    private WebSocketListenerHandler webSocketListenerHandler;
    @Value("${longan.api.enable:false}")
    private boolean apiEnabled;
    private boolean dubboEnabled;
    private final AntPathMatcher matcher = new AntPathMatcher();
    private final Map<RequestMethod, List<String>> httpStaticMappings = new HashMap<>();
    private final Map<RequestMethod, List<String>> httpDynamicMappings = new HashMap<>();

    {
        for (RequestMethod requestMethod : RequestMethod.values()) {
            httpStaticMappings.put(requestMethod, new ArrayList<>());
            httpDynamicMappings.put(requestMethod, new ArrayList<>());
        }
    }

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof LonganListableBeanFactory longanListableBeanFactory) {
            dubboEnabled = longanListableBeanFactory.isDubboEnabled();
        }
        String[] candidateNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors((ListableBeanFactory) beanFactory, WebSocketListener.class);
        for (String candidateName : candidateNames) {
            webSocketListenerHandler.addWebSocketListener((WebSocketListener) beanFactory.getBean(candidateName));
        }
        if (dubboEnabled) {
            this.dubboInterceptor = new DubboInterceptor();
        }
    }

    @Override
    public void afterPropertiesSet() {
        exportRpcService();
        loadApiDocument();
    }

    private void exportRpcService() {
        List<ServiceDescriptor> serviceDescriptorList = ClassPathBeanScanner.getServiceClassList();
        for (ServiceDescriptor serviceDescriptor : serviceDescriptorList) {
            Class<?> serviceClass = serviceDescriptor.getServiceClass();
            List<MethodDescriptor> methodDescriptors = serviceDescriptor.getMethodDescriptorList();
            if (serviceClass.isAnnotationPresent(RpcService.class)) {
                exportRpcService(serviceClass, methodDescriptors);
            }
        }
    }

    private Class<?> getCandidateInterface(Class<?>[] serviceInterfaces, Method method) {
        for (Class<?> serviceInterface : serviceInterfaces) {
            if (ClassUtils.hasMethod(serviceInterface, method)) {
                return serviceInterface;
            }
        }
        return null;
    }

    private void exportRpcService(Class<?> serviceClass, List<MethodDescriptor> methodDescriptors) {
        for (MethodDescriptor methodDescriptor : methodDescriptors) {
            Method method = methodDescriptor.getMethod();
            if (method.isAnnotationPresent(Mapping.class)) {
                Mapping mapping = method.getAnnotation(Mapping.class);

                Channel[] channels = mapping.channel();
                boolean httpChannelEnabled = false;
                boolean dubboChannelEnabled = false;
                for (Channel channel : channels) {
                    if (channel == Channel.HTTP) {
                        httpChannelEnabled = true;
                    } else if (channel == Channel.DUBBO) {
                        dubboChannelEnabled = true;
                    }
                }
                if (dubboEnabled && dubboChannelEnabled) {
                    Class<?>[] serviceInterfaces = serviceClass.getInterfaces();
                    Class<?> serviceInterface = getCandidateInterface(serviceInterfaces, method);
                    if (serviceInterface != null) {
                        exportDubboService(serviceClass, method, methodDescriptor);
                    }
                }
                if (httpChannelEnabled) {
                    exportHttpService(serviceClass, mapping, methodDescriptor);
                }
            }
        }
    }

    private void exportDubboService(Class<?> serviceClass, Method method, MethodDescriptor methodDescriptor) {
        Class<?>[] serviceInterfaces = serviceClass.getInterfaces();
        Class<?> serviceInterface = getCandidateInterface(serviceInterfaces, method);
        if (serviceInterface != null) {
            dubboInterceptor.put(method, serviceInterface.getName() + "." + method.getName(), methodDescriptor);

            ServiceConfig<Object> serviceConfig = new ServiceConfig<>();
            serviceConfig.setInterface(serviceInterface);
            serviceConfig.setRef(context.getBean(serviceClass));
            serviceConfig.setFilter(DubboFilter.class.getName());
            serviceConfig.export();
        }
    }

    private void exportHttpService(Class<?> serviceClass, Mapping mapping, MethodDescriptor methodDescriptor) {
        String servicePath = serviceClass.getAnnotation(RpcService.class).path();
        if (servicePath.isEmpty()) {
            Class<?> modelClass = getModelClass(serviceClass);
            servicePath = "/" + StringUtils.lowerFirst(modelClass.getSimpleName());
        }
        String methodPath = mapping.path();
        if (methodPath.isEmpty()) {
            methodPath = methodDescriptor.getMethod().getName();
        }
        RequestMethod requestMethod = mapping.method();
        Object service = context.getBean(serviceClass);
        List<String> httpStaticMappingList = httpStaticMappings.get(requestMethod);
        List<String> httpDynamicMappingList = httpDynamicMappings.get(requestMethod);
        String path = servicePath + "/" + methodPath;
        path = path.replaceAll("/+", "/");
        if (matcher.isPattern(path)) {
            for (String existedMapping : httpStaticMappingList) {
                if (matcher.match(path, existedMapping)) {
                    throw new RuntimeException("More than one " + path + " mapping was found with " + serviceClass.getName());
                }
            }
            for (String existedMapping : httpDynamicMappingList) {
                if (matcher.match(path, existedMapping)) {
                    throw new RuntimeException("More than one " + path + " mapping was found with " + serviceClass.getName());
                }
            }
            httpDynamicMappingList.add(path);
            httpServiceInvoker.addDynamicMethod(service, methodDescriptor, requestMethod, path);
        } else {
            for (String existedMapping : httpStaticMappingList) {
                if (existedMapping.endsWith(path)) {
                    throw new RuntimeException("More than one " + path + " mapping was found with " + serviceClass.getName());
                }
            }
            for (String existedMapping : httpDynamicMappingList) {
                if (matcher.match(existedMapping, path)) {
                    throw new RuntimeException("More than one " + path + " mapping was found with " + serviceClass.getName());
                }
            }
            httpStaticMappingList.add(path);
            httpServiceInvoker.addStaticMethod(service, methodDescriptor, requestMethod, path);
        }
    }

    private void loadApiDocument() {
        if (apiEnabled) {
            DocumentService.check();
        }
    }

    private Class<?> getModelClass(Class<?> serviceClass) {
        Type genericSuperclass = serviceClass.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType parameterizedType) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            for (Type actualTypeArgument : actualTypeArguments) {
                if (((Class<?>) actualTypeArgument).isAnnotationPresent(Entity.class)) {
                    return (Class<?>) actualTypeArgument;
                }
            }
        }
        throw new RuntimeException("The " + serviceClass.getName() + " was not RpcService");
    }
}
