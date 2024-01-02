package dev.paoding.longan.util;

import dev.paoding.longan.service.SystemException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TypeUtils {
    private static Map<String, Field> nameFieldMapCache = new ConcurrentHashMap<>();
    private static Map<Class<?>, List<Field>> typeFieldMapCache = new ConcurrentHashMap<>();

    public static String getLowerSimpleName(Class<?> type) {
        return StringUtils.underline(type.getSimpleName()).toLowerCase();
    }

    public static String getUpperSimpleName(Class<?> type) {
        return StringUtils.underline(type.getSimpleName()).toUpperCase();
    }

    public static List<Field> getDeclaredFields(Class<?> type) {
        if (typeFieldMapCache.containsKey(type)) {
            return typeFieldMapCache.get(type);
        }
        List<Field> fieldList = new ArrayList<>();
        loadFiled(fieldList, type);
        return fieldList;
    }

    private static void loadFiled(List<Field> fieldList, Class<?> type) {
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            int modifier = field.getModifiers();
            if (modifier == Modifier.PRIVATE || modifier == Modifier.PUBLIC || modifier == Modifier.PROTECTED) {
                field.setAccessible(true);
                fieldList.add(field);
            }
        }
        if (type.getSuperclass() != null) {
            loadFiled(fieldList, type.getSuperclass());
        }
    }

    public static Field getField(Class<?> type, String filedName) {
        String typeName = type.getTypeName();
        String key = typeName + "." + filedName;
        if (nameFieldMapCache.containsKey(key)) {
            return nameFieldMapCache.get(key);
        }
        loadFiled(typeName, type);
        return nameFieldMapCache.get(key);
    }

    private static void loadFiled(String typeName, Class<?> type) {
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            int modifier = field.getModifiers();
            if (modifier == Modifier.PRIVATE || modifier == Modifier.PUBLIC || modifier == Modifier.PROTECTED) {
                nameFieldMapCache.put(typeName + "." + field.getName(), field);
            }
        }
        if (type.getSuperclass() != null) {
            loadFiled(typeName, type.getSuperclass());
        }
    }

    public static <T> List<String> getEnumValues(Class<T> enumType) {
        List<String> values = new ArrayList<>();
        try {
            Method method = enumType.getDeclaredMethod("values");
            T[] array = (T[]) method.invoke(null, null);
            for (int i = 0; i < array.length; i++) {
                values.add(array[i].toString());
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            throw new SystemException(e.getMessage());
        }
        return values;
    }

}