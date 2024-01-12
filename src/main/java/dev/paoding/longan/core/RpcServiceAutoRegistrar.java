package dev.paoding.longan.core;

import dev.paoding.longan.annotation.Mapping;
import dev.paoding.longan.annotation.RpcService;
import dev.paoding.longan.channel.Channel;
import dev.paoding.longan.channel.dubbo.DubboInterceptor;
import dev.paoding.longan.channel.http.DefaultHandlerInterceptor;
import dev.paoding.longan.channel.http.HandlerInterceptor;
import dev.paoding.longan.channel.http.MethodInvocationProvider;
import dev.paoding.longan.channel.http.RequestMethod;
import dev.paoding.longan.data.Entity;
import dev.paoding.longan.util.StringUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class RpcServiceAutoRegistrar implements ImportBeanDefinitionRegistrar {
    private final AntPathMatcher matcher = new AntPathMatcher();
    private final Map<RequestMethod, List<String>> httpStaticMappings = new HashMap<>();
    private final Map<RequestMethod, List<String>> httpDynamicMappings = new HashMap<>();
    private final MethodInvocationProvider methodInvocationProvider;
    private DubboInterceptor dubboInterceptor;
    private boolean dubboEnabled;

    {
        for (RequestMethod requestMethod : RequestMethod.values()) {
            httpStaticMappings.put(requestMethod, new ArrayList<>());
            httpDynamicMappings.put(requestMethod, new ArrayList<>());
        }
        methodInvocationProvider = new MethodInvocationProvider();
    }

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry) {
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) registry;
        SortedSet<String> candidatePackages = new TreeSet<>();
        String[] beanDefinitionNames = defaultListableBeanFactory.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = defaultListableBeanFactory.getBeanDefinition(beanDefinitionName);
            if (beanDefinition instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
                Map<String, Object> annotationAttributeMap = annotatedBeanDefinition.getMetadata().getAnnotationAttributes(ComponentScan.class.getName());
                if (annotationAttributeMap != null && beanDefinition.getBeanClassName() != null) {
                    candidatePackages.addAll(Arrays.asList((String[]) annotationAttributeMap.get("basePackages")));
                    candidatePackages.add(ClassUtils.getPackageName(beanDefinition.getBeanClassName()));
                }
            }
        }

        ClassPathBeanScanner classPathBeanScanner = new ClassPathBeanScanner(candidatePackages);
        defaultListableBeanFactory.registerSingleton(ClassPathBeanScanner.class.getSimpleName(), classPathBeanScanner);
        defaultListableBeanFactory.registerSingleton("methodInvocationProvider", methodInvocationProvider);

        if (ClassPathBeanScanner.getHandlerInterceptor() == null) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(HandlerInterceptor.class);
            AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
            beanDefinition.setBeanClass(DefaultHandlerInterceptor.class);
            registry.registerBeanDefinition(HandlerInterceptor.class.getSimpleName(), beanDefinition);
        }

        if (registry instanceof LonganListableBeanFactory longanListableBeanFactory) {
            dubboEnabled = longanListableBeanFactory.isDubboEnabled();
            if (dubboEnabled) {
                this.dubboInterceptor = new DubboInterceptor();
                longanListableBeanFactory.registerSingleton("dubboInterceptor", dubboInterceptor);

//                ProviderConfig providerConfig = new ProviderConfig();
//                providerConfig.setTimeout(12000);
//                providerConfig.setRetries(0);
//                providerConfig.setGroup("provider-group");
//                providerConfig.setVersion("1.0.0");
//                providerConfig.setFilter("-exception");
//                providerConfig.setFilter(DubboFilter.class.getName());
//                longanListableBeanFactory.registerSingleton("providerConfig", providerConfig);
            }
        }



        List<ServiceDescriptor> serviceDescriptorList = ClassPathBeanScanner.getServiceClassList();
        for (ServiceDescriptor serviceDescriptor : serviceDescriptorList) {
            Class<?> serviceClass = serviceDescriptor.getServiceClass();
            List<MethodDescriptor> methodDescriptors = serviceDescriptor.getMethodDescriptorList();
            if (serviceClass.isAnnotationPresent(RpcService.class)) {
                exportRpcService(serviceClass, methodDescriptors);
            }
        }
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
                    exportDubboService(serviceClass, methodDescriptor);
                }
                if (httpChannelEnabled) {
                    exportHttpService(serviceClass, mapping, methodDescriptor);
                }
            }
        }
    }

    private void exportDubboService(Class<?> serviceClass, MethodDescriptor methodDescriptor) {
        Class<?>[] serviceInterfaces = serviceClass.getInterfaces();
        Method method = methodDescriptor.getMethod();
        Class<?> serviceInterface = getCandidateInterface(serviceInterfaces, method);
        if (serviceInterface != null) {
            MethodInvocation methodInvocation = new MethodInvocation(serviceInterface, serviceClass, methodDescriptor);
            dubboInterceptor.add(methodInvocation);
            methodInvocationProvider.addDubboMethod(methodInvocation);
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
            methodInvocationProvider.addDynamicHttpMethod(methodDescriptor, requestMethod, path);
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
            methodInvocationProvider.addStaticHttpMethod(methodDescriptor, requestMethod, path);
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
