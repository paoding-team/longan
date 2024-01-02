package dev.paoding.longan.util;

import dev.paoding.longan.data.jpa.*;
import dev.paoding.longan.service.SystemException;
import org.springframework.util.ClassUtils;

import java.lang.reflect.*;
import java.util.List;

public class EntityUtils {

    public static Object get(Field field, Object object) {
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new SystemException(e.getMessage());
        }
    }

    public static Object getId(Object bean) {
        Method method = ClassUtils.getMethod(bean.getClass(), "getId");
        try {
            return method.invoke(bean);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setId(Object bean, Object id, Class<?> type) {
        Method method = ClassUtils.getMethod(bean.getClass(), "setId", type);
        try {
            method.invoke(bean, id);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> wrap(MetaTable<T> metaTable, List<T> beanList) {
        Class<T> master = metaTable.getType();
        List<Field> oneToManyFieldList = metaTable.getOneToManyFieldList();
        for (Field field : oneToManyFieldList) {
            field.setAccessible(true);
            Type type = field.getGenericType();
            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            String joinFile = oneToMany.joinField();
            Class<?> slaver = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
            for (T bean : beanList) {
                try {
                    field.set(bean, BeanFactory.createList(master, slaver, null, joinFile, bean));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        List<Field> manyToManyFieldList = metaTable.getManyToManyFieldList();
        for (Field field : manyToManyFieldList) {
            field.setAccessible(true);
            Type type = field.getGenericType();
            Role role = null;
            ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
            if (!manyToMany.role().isEmpty()) {
                role = Role.of(manyToMany.role());
            }
            Class<?> slaver = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
            for (T bean : beanList) {
                try {
                    field.set(bean, BeanFactory.createList(master, slaver, role, "", bean));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return beanList;
    }


    public static <T> T wrap(MetaTable<T> metaTable, T bean) {
        Class<T> master = metaTable.getType();
        List<Field> oneToManyFieldList = metaTable.getOneToManyFieldList();
        for (Field field : oneToManyFieldList) {
            field.setAccessible(true);
            Type type = field.getGenericType();
            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            String joinFile = oneToMany.joinField();
            Class<?> slaver = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
            try {
                field.set(bean, BeanFactory.createList(master, slaver, null, joinFile, bean));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        List<Field> manyToManyFieldList = metaTable.getManyToManyFieldList();
        for (Field field : manyToManyFieldList) {
            field.setAccessible(true);
            Type type = field.getGenericType();
            Role role = null;
            ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
            if (!manyToMany.role().isEmpty()) {
                role = Role.of(manyToMany.role());
            }
            Class<?> slaver = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
            try {
                field.set(bean, BeanFactory.createList(master, slaver, role, "", bean));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return bean;
    }
}
