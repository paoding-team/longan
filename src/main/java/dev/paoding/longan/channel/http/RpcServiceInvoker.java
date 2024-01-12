package dev.paoding.longan.channel.http;

import com.google.gson.JsonElement;
import dev.paoding.longan.annotation.Param;
import dev.paoding.longan.annotation.Validator;
import dev.paoding.longan.core.MethodInvocation;
import dev.paoding.longan.core.ServiceInvoker;
import dev.paoding.longan.service.MethodNotFoundException;
import dev.paoding.longan.data.Between;
import dev.paoding.longan.util.GsonUtils;
import dev.paoding.longan.validation.BeanCleaner;
import dev.paoding.longan.validation.BeanValidator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RpcServiceInvoker extends ServiceInvoker {
    private final Map<String, MethodInvocation> rpcMethodMap = new ConcurrentHashMap<>();
    private final BeanValidator beanValidator = new BeanValidator();
    private final BeanCleaner beanCleaner = new BeanCleaner();

//    public void addRpcMethod(Object rpcService, Method method, String mapping) {
//        MethodInvocation methodInvocation = new MethodInvocation();
//        methodInvocation.setService(rpcService);
//        methodInvocation.setMethod(method);
//        methodInvocation.setPath(mapping);
//        rpcMethodMap.put(mapping, methodInvocation);
//    }

    public Object invoke(String path, MultipartFile multipartFile) {
        MethodInvocation methodInvocation = rpcMethodMap.get(path);
        if (methodInvocation == null) {
            throw new MethodNotFoundException(path + " not found");
        }
        Object[] objects = new Object[1];
        objects[0] = multipartFile;
        Object object = methodInvocation.getService();
        Method method = methodInvocation.getMethod();
        return invoke(method, object, objects);
    }

    public Object invokeRpcService(String path, Map<String, JsonElement> jsonElementMap) {
        MethodInvocation methodInvocation = rpcMethodMap.get(path);
        if (methodInvocation == null) {
            throw new MethodNotFoundException(path + " not found");
        }

        Map<String, Validator> validatorMap = methodInvocation.getValidatorMap();
        Map<String, Param> paramMap = methodInvocation.getParamMap();
        Parameter[] parameters = methodInvocation.getParameters();
        Object[] objects = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            JsonElement element = jsonElementMap.get(parameter.getName());
            Object value = null;
            if (element != null) {
                value = GsonUtils.fromJson(element, parameter.getParameterizedType());
                if (Between.class.isAssignableFrom(parameter.getType())) {
                    Between<?> between = (Between<?>) value;
                    between.setField(parameter.getName());
                }
            }

            if (paramMap.containsKey(parameter.getName())) {
                Param param = paramMap.get(parameter.getName());
                beanValidator.validateParameter(parameter, value, param, validatorMap);
                beanCleaner.cleanParameter(parameter, value, param, validatorMap);
            }

            objects[i] = value;
        }

        return invoke(methodInvocation.getMethod(), methodInvocation.getService(), objects);
    }
}
