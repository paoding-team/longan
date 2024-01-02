package dev.paoding.longan.core;

import dev.paoding.longan.annotation.LonganBootApplication;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

public class OnBeanCondition implements Condition {
    @Override
    public boolean matches(@NonNull ConditionContext context, AnnotatedTypeMetadata metadata) {
        MultiValueMap<String, Object> attributes = metadata
                .getAllAnnotationAttributes(ConditionalOnMissingBean.class.getName(), true);
        if (null == attributes) {
            return false;
        }
        List<?> list = attributes.get("value");
        if (list.size() == 1) {
            String className = list.get(0).toString();
            int result = matches(forName(className));
            if (result == 3) {
                return false;
            }
//            if (AnnotationMetadata.class.isAssignableFrom(metadata.getClass())) {
//                AnnotationMetadata annotationMetadata = (AnnotationMetadata) metadata;
//                if (isAssignableFrom(className, annotationMetadata.getClassName())) {
//                    return true;
//                } else {
//                    return false;
//                }
//            }
            if (MethodMetadata.class.isAssignableFrom(metadata.getClass())) {
                if (result == 2) {
                    return false;
                }
            }
        }
        return true;
    }


    private int matches(Class<?> type) {
//        List<Class<?>> beanClassList = BeanClassLoader.loadAllClass();
//        int i = 1;
//        for (Class<?> beanClass : beanClassList) {
//            if (type.isAssignableFrom(beanClass) && (beanClass.isAnnotationPresent(Component.class) || beanClass.isAnnotationPresent(Service.class))) {
//                if (beanClass.isAnnotationPresent(ConditionalOnMissingBean.class)) {
//                    i = 2;
//                } else {
//                    return 3;
//                }
//            }
//        }
//        return i;
        return 0;
    }

    private boolean isAssignableFrom(String parent, String child) {
        return forName(parent).isAssignableFrom(forName(child));
    }

    private Class<?> forName(String name) {
        try {
            return ClassUtils.forName(name, null);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    private String findBootPackageName(ConditionContext context) {
        Map<String, Object> annotatedBeans = context.getBeanFactory().getBeansWithAnnotation(LonganBootApplication.class);
        return annotatedBeans.isEmpty() ? null : annotatedBeans.values().toArray()[0].getClass().getPackage().getName();
    }
}
