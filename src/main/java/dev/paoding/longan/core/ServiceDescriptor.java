package dev.paoding.longan.core;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ServiceDescriptor {
    private final List<MethodDescriptor> methodDescriptorList = new ArrayList<>();
    private final Class<?> serviceClass;

    public ServiceDescriptor(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
        Method[] methods = serviceClass.getDeclaredMethods();
        for (Method method : methods) {
            methodDescriptorList.add(new MethodDescriptor(method));
        }

        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass;
        try {
            ctClass = classPool.get(serviceClass.getName());
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
        CtMethod[] ctMethods = ctClass.getDeclaredMethods();
        for (int i = 0; i < ctMethods.length; i++) {
            int lineNumber = ctMethods[i].getMethodInfo().getLineNumber(0);
            methodDescriptorList.get(i).setLineNumber(lineNumber);
        }
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return serviceClass.isAnnotationPresent(annotationClass);
    }

    public Class<?> getServiceClass() {
        return serviceClass;
    }

    public List<MethodDescriptor> getMethodDescriptorList() {
        return methodDescriptorList;
    }

}
