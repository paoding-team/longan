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
public class MethodInvocationProvider {
    private final Map<HttpMethod, List<MethodInvocation>> dynamicHttpMethodInvocations = new ConcurrentHashMap<>();
    private final Map<HttpMethod, Map<String, MethodInvocation>> staticHttpMethodInvocations = new ConcurrentHashMap<>();
    private final List<MethodInvocation> dubboMethodInvocations = new ArrayList<>();
    private final AntPathMatcher matcher = new AntPathMatcher();

    {
        for (RequestMethod requestMethod : RequestMethod.values()) {
            dynamicHttpMethodInvocations.put(HttpMethod.valueOf(requestMethod.name()), new ArrayList<>());
            staticHttpMethodInvocations.put(HttpMethod.valueOf(requestMethod.name()), new ConcurrentHashMap<>());
        }
    }

    public List<MethodInvocation> getHttpMethodInvocations() {
        List<MethodInvocation> methodInvocations = new ArrayList<>();

        for (List<MethodInvocation> dynamicMethodInvocations : dynamicHttpMethodInvocations.values()) {
            methodInvocations.addAll(dynamicMethodInvocations);
        }

        for (Map<String, MethodInvocation> staticMethodInvocations : staticHttpMethodInvocations.values()) {
            methodInvocations.addAll(staticMethodInvocations.values());
        }
        return methodInvocations;
    }

    public List<MethodInvocation> getDubboMethodInvocations(){
        return dubboMethodInvocations;
    }

    public void addDubboMethod(MethodInvocation methodInvocation) {
        this.dubboMethodInvocations.add(methodInvocation);
    }

    public void addStaticHttpMethod(MethodDescriptor methodDescriptor, RequestMethod requestMethod, String path) {
        MethodInvocation methodInvocation = new MethodInvocation(methodDescriptor, path);
        staticHttpMethodInvocations.get(HttpMethod.valueOf(requestMethod.name())).put(path, methodInvocation);
    }

    public void addDynamicHttpMethod(MethodDescriptor methodDescriptor, RequestMethod requestMethod, String path) {
        MethodInvocation methodInvocation = new MethodInvocation(methodDescriptor, path);
        dynamicHttpMethodInvocations.get(HttpMethod.valueOf(requestMethod.name())).add(methodInvocation);
    }

    protected MethodInvocation get(HttpMethod httpMethod, String path) {
        if (staticHttpMethodInvocations.get(httpMethod).containsKey(path)) {
            return staticHttpMethodInvocations.get(httpMethod).get(path);
        }
        for (MethodInvocation methodInvocation : dynamicHttpMethodInvocations.get(httpMethod)) {
            String pattern = methodInvocation.getPath();
            if (matcher.match(pattern, path)) {
                return methodInvocation;
            }
        }
        throw new MethodNotFoundException(path + " not found");
    }
}
