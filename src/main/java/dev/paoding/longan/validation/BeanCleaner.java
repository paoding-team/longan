package dev.paoding.longan.validation;

import dev.paoding.longan.annotation.Param;
import dev.paoding.longan.annotation.Validate;
import dev.paoding.longan.annotation.Validator;
import dev.paoding.longan.service.SystemException;
import dev.paoding.longan.data.Entity;
import dev.paoding.longan.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanCleaner {

    /**
     * 删除多余的参数，Param中的没有定义的Prop都会被删除。
     */
    public void cleanParameter(Parameter parameter, Object object, Param param, Map<String, Validator> validatorMap) {
        if (object == null) return;
        Class<?> type = parameter.getType();
        if (type.isAnnotationPresent(Entity.class)) {
            cleanEntity(type, object, param.validator(), validatorMap);
        } else if (Collection.class.isAssignableFrom(type) && parameter.getParameterizedType() instanceof ParameterizedType parameterizedType) {
            cleanCollection(parameterizedType, object, param.validator(), validatorMap);
        }
    }

    private void cleanCollection(ParameterizedType parameterizedType, Object object, int validatorId, Map<String, Validator> validatorMap) {
        Class<?> type = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        if (type.isAnnotationPresent(Entity.class)) {
            Collection<?> collection = (Collection<?>) object;
            for (Object item : collection) {
                cleanEntity(type, item, validatorId, validatorMap);
            }
        }
    }

    private void cleanEntity(Class<?> type, Object object, int validatorId, Map<String, Validator> validatorMap) {
        Validator validator = validatorMap.get(type.getName() + validatorId);
        Validate[] validates = validator.validates();
        Map<String, Validate> propMap = new HashMap<>();
        for (Validate validate : validates) {
            propMap.put(validate.name(), validate);
        }
        List<Field> fieldList = TypeUtils.getDeclaredFields(type);
        try {
            for (Field field : fieldList) {
                if (propMap.containsKey(field.getName())) {
                    Class<?> fieldType = field.getType();
                    if (fieldType.isAnnotationPresent(Entity.class)) {
                        cleanEntity(field.getType(), field.get(object), propMap.get(field.getName()).validator(), validatorMap);
                    } else if (Collection.class.isAssignableFrom(fieldType) && field.getGenericType() instanceof ParameterizedType parameterizedType) {
                        cleanCollection(parameterizedType, field.get(object), propMap.get(field.getName()).validator(), validatorMap);
                    }
                } else {
                    if (field.getModifiers() == Modifier.PRIVATE || field.getModifiers() == Modifier.PUBLIC || field.getModifiers() == Modifier.PROTECTED) {
                        Class<?> fieldType = field.getType();
                        if (fieldType.isPrimitive()) {
                            if (boolean.class.isAssignableFrom(fieldType)) {
                                field.set(object, false);
                            } else if (int.class.isAssignableFrom(fieldType) || long.class.isAssignableFrom(fieldType) || double.class.isAssignableFrom(fieldType) ||
                                    float.class.isAssignableFrom(fieldType) || short.class.isAssignableFrom(fieldType)) {
                                field.set(object, 0);
                            }
                        } else {
                            field.set(object, null);
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new SystemException(e.getMessage());
        }
        propMap.clear();
    }
}
