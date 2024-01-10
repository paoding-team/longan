package dev.paoding.longan.channel.http;

import dev.paoding.longan.core.MethodDescriptor;
import dev.paoding.longan.core.MethodInvocation;
import dev.paoding.longan.service.MethodNotFoundException;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MethodInvocationStore {
    private final Map<HttpMethod, List<MethodInvocation>> dynamicMethodMap = new ConcurrentHashMap<>();
    private final Map<HttpMethod, Map<String, MethodInvocation>> staticMethodMap = new ConcurrentHashMap<>();
    private final AntPathMatcher matcher = new AntPathMatcher();

    {
        for (RequestMethod requestMethod : RequestMethod.values()) {
            dynamicMethodMap.put(HttpMethod.valueOf(requestMethod.name()), new ArrayList<>());
            staticMethodMap.put(HttpMethod.valueOf(requestMethod.name()), new ConcurrentHashMap<>());
        }
    }

    public void addStaticMethod(Object service, MethodDescriptor methodDescriptor, RequestMethod requestMethod, String path) {
        MethodInvocation methodInvocation = createMethodWrap(service, methodDescriptor, path);
        staticMethodMap.get(HttpMethod.valueOf(requestMethod.name())).put(path, methodInvocation);
    }

    public void addDynamicMethod(Object service, MethodDescriptor methodDescriptor, RequestMethod requestMethod, String path) {
        MethodInvocation methodInvocation = createMethodWrap(service, methodDescriptor, path);
        dynamicMethodMap.get(HttpMethod.valueOf(requestMethod.name())).add(methodInvocation);
    }

    private MethodInvocation createMethodWrap(Object service, MethodDescriptor methodDescriptor, String path) {
        MethodInvocation methodInvocation = new MethodInvocation();
        methodInvocation.setService(service);
        methodInvocation.setPath(path);
        methodInvocation.setMethod(methodDescriptor.getMethod());
        methodInvocation.setLineNumber(methodDescriptor.getLineNumber());
        return methodInvocation;
    }

    protected MethodInvocation get(HttpMethod httpMethod, String path) {
        if (staticMethodMap.get(httpMethod).containsKey(path)) {
            return staticMethodMap.get(httpMethod).get(path);
        }
        for (MethodInvocation methodInvocation : dynamicMethodMap.get(httpMethod)) {
            String pattern = methodInvocation.getPath();
            if (matcher.match(pattern, path)) {
                return methodInvocation;
            }
        }
        throw new MethodNotFoundException(path + " not found");
    }
}
