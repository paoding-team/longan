package dev.paoding.longan.validation;

import dev.paoding.longan.annotation.Param;
import dev.paoding.longan.annotation.Validate;
import dev.paoding.longan.annotation.Validator;
import dev.paoding.longan.service.ConstraintViolationException;
import dev.paoding.longan.service.InternalServerException;
import dev.paoding.longan.data.Entity;
import dev.paoding.longan.util.TypeUtils;
import org.springframework.cglib.beans.BeanMap;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BeanValidator {

    public void validateParameter(Parameter parameter, Object object, Param param, Map<String, Validator> validatorMap) {
        Class<?> type = parameter.getType();
        if (param.notNull()) {
            validateNotNull(type, object, param.name());
        }
        if (param.notBlank()) {
            validateNotBlank(type, object, param.name());
        }
        if (param.notEmpty()) {
            validateNotEmpty(type, object, param.name());
        }

        if (param.size().length == 2) {
            long[] sizeArray = param.size();
            long min = sizeArray[0];
            long max = sizeArray[1];
            validateSize(type, object, min, max, param.name());
        }

        if (!param.regexp().isEmpty()) {
            validateRegex(object, param.name(), param.regexp());
        }

        if (object == null) {
            return;
        }

        if (type.isAnnotationPresent(Entity.class)) {
            validateEntity(type, object, param.name(), param.validator(), validatorMap);
        } else if (Collection.class.isAssignableFrom(type) && parameter.getParameterizedType() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
            validateCollection(parameterizedType, param.name(), object, param.validator(), validatorMap);
        }
    }

    private void validateFiled(Field field, Object object, String qualifiedName, Validate validate, Map<String, Validator> validatorMap) {
        Class<?> type = field.getType();
        qualifiedName = qualifiedName + "." + validate.name();
        if (validate.notNull()) {
            validateNotNull(type, object, qualifiedName);
        }
        if (validate.notBlank()) {
            validateNotBlank(type, object, qualifiedName);
        }

        if (validate.notEmpty()) {
            validateNotEmpty(type, object, qualifiedName);
        }

        if (validate.size().length == 2) {
            long[] sizeArray = validate.size();
            long min = sizeArray[0];
            long max = sizeArray[1];
            validateSize(type, object, min, max, qualifiedName);
        }

        if (!validate.regexp().isEmpty()) {
            validateRegex(object, qualifiedName, validate.regexp());
        }

        if (type.isAnnotationPresent(Entity.class)) {
            validateEntity(type, object, qualifiedName, validate.validator(), validatorMap);
        } else if (Collection.class.isAssignableFrom(type) && field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            validateCollection(parameterizedType, qualifiedName, object, validate.validator(), validatorMap);
        }
    }

    /**
     * 验证集合类对象
     *
     * @param parameterizedType
     * @param qualifiedName
     * @param object
     * @param validatorMap
     */
    private void validateCollection(ParameterizedType parameterizedType, String qualifiedName, Object object, int validatorId, Map<String, Validator> validatorMap) {
        Class<?> type = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        if (type.isAnnotationPresent(Entity.class)) {
            Collection<?> collection = (Collection<?>) object;
            for (Object item : collection) {
                validateEntity(type, item, qualifiedName, validatorId, validatorMap);
            }
        }
    }

    /**
     * 验证 Entity 对象
     *
     * @param type
     * @param object
     * @param qualifiedName
     * @param validatorMap
     */
    private void validateEntity(Class<?> type, Object object, String qualifiedName, int validatorId, Map<String, Validator> validatorMap) {
        BeanMap beanMap = BeanMap.create(object);
        Validator validator = validatorMap.get(type.getName() + validatorId);
        //todo 等增加了启动检查，删除这个检查
        if (validator == null) {
            throw new InternalServerException("not found @Validator for " + type.getName());
        }
        for (Validate validate : validator.validates()) {
            Field field = TypeUtils.getField(type, validate.name());
            validateFiled(field, beanMap.get(validate.name()), qualifiedName, validate, validatorMap);
        }
    }


    private void validateNotNull(Class<?> type, Object object, String name) {
        if (object == null) {
            String message;
            if (Enum.class.isAssignableFrom(type)) {
                List<String> values = TypeUtils.getEnumValues(type);
                message = "The '" + name + "' parameter must be in " + values;
            } else {
                message = "The '" + name + "' parameter must be not null";
            }
            throw new ConstraintViolationException(name + ".not.null", message);
        }
    }

    private void validateNotBlank(Class<?> type, Object object, String name) {
        validateNotNull(type, object, name);
        if (CharSequence.class.isAssignableFrom(object.getClass()) && object.toString().trim().length() == 0) {
            String message = "The '" + name + "' parameter must contain at least one non-whitespace character";
            throw new ConstraintViolationException(name + ".not.blank", message);
        }
    }

    private void validateNotEmpty(Class<?> type, Object object, String name) {
        validateNotNull(type, object, name);
        if (size(object.getClass(), object) == 0) {
            String message = "The '" + name + "' parameter must be not empty";
            throw new ConstraintViolationException(name + ".not.empty", message);
        }
    }

    private void validateSize(Class<?> type, Object object, long min, long max, String name) {
        validateNotNull(type, object, name);
        if (!validateSize(type, object, min, max)) {
            String message;
            if (CharSequence.class.isAssignableFrom(type)) {
                message = "length of " + name;
            } else if (Collection.class.isAssignableFrom(type)) {
                message = name + " size";
            } else if (type.isArray()) {
                message = name + " length";
            } else if (Map.class.isAssignableFrom(type)) {
                message = name + " size";
            } else {
                message = name;
            }
            message += " must be greater than or equal to " + min + " and less than or equal to " + max;

            throw new ConstraintViolationException(name + ".not.range", message);
        }
    }

    private void validateRegex(Object object, String name, String regex) {
        if (object == null) {
            return;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(object.toString());
        if (!matcher.find()) {
            String message = "The '" + name + "' parameter must match " + regex;
            throw new ConstraintViolationException(name + ".not.match", message);
        }
    }

    private int size(Class<?> type, Object object) {
        if (CharSequence.class.isAssignableFrom(type)) {
            return object.toString().length();
        }
        if (Collection.class.isAssignableFrom(type)) {
            return ((Collection<?>) object).size();
        }
        if (type.isArray()) {
            return Array.getLength(object);
        }
        if (Map.class.isAssignableFrom(type)) {
            return ((Map<?, ?>) object).size();
        }
        return 0;
    }

    public boolean validateSize(Class<?> type, Object object, long min, long max) {
        if (Number.class.isAssignableFrom(type)) {
            if (Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)) {
                double size = (double) object;
                return size >= min && size <= max;
            } else if (Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type)) {
                long size = (long) object;
                return size >= min && size <= max;
            } else if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) {
                int size = (int) object;
                return size >= min && size <= max;
            } else if (Short.class.isAssignableFrom(type) || short.class.isAssignableFrom(type)) {
                short size = (short) object;
                return size >= min && size <= max;
            }
        } else {
            int size = size(type, object);
            return size >= min && size <= max;
        }
        return true;
    }
}

