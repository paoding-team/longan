package dev.paoding.longan.core;

import org.apache.dubbo.config.ReferenceConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.lang.annotation.Annotation;
import java.util.Set;

public class LonganListableBeanFactory extends DefaultListableBeanFactory {
    private boolean dubboEnabled;

    public LonganListableBeanFactory(Class<?> primarySource) {
        Annotation[] annotations = primarySource.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().getName().equals("org.apache.dubbo.config.spring.context.annotation.EnableDubbo")) {
                dubboEnabled = true;
                break;
            }
        }
    }

    public boolean isDubboEnabled() {
        return dubboEnabled;
    }

    @Override
    public Object doResolveDependency(DependencyDescriptor descriptor, @Nullable String beanName,
                                      @Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {
        try {
            return super.doResolveDependency(descriptor, beanName, autowiredBeanNames, typeConverter);
        } catch (NoSuchBeanDefinitionException e) {
            if (dubboEnabled) {
                return doResolveDependency(descriptor);
            }
            throw e;
        }
    }

    private Object doResolveDependency(DependencyDescriptor descriptor) {
        ResolvableType resolvableType = descriptor.getResolvableType();
        Class<?> type = resolvableType.resolve();
        if (type != null && type.isInterface()) {
            ReferenceConfig<?> reference = new ReferenceConfig<>();
            reference.setInterface(type);
            return reference.get();
        }
        throw new NoSuchBeanDefinitionException(resolvableType,
                "expected at least 1 bean which qualifies as autowire candidate. " +
                        "Dependency annotations: " + ObjectUtils.nullSafeToString(descriptor.getAnnotations()));
    }
}
