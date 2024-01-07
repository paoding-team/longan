package dev.paoding.longan.data.jpa;

import dev.paoding.longan.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cglib.proxy.Enhancer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BeanFactory {
    private static JdbcSession jdbcSession;

    public static void register(JdbcSession jdbcSession) {
        BeanFactory.jdbcSession = jdbcSession;
    }

    public static <T> T create(Class<T> clazz, Object id) {
        if (MetaTableFactory.contains(clazz)) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(clazz);
            enhancer.setUseCache(true);
            enhancer.setInterceptDuringConstruction(false);
            enhancer.setInterfaces(new Class[]{BeanProxy.class});
            enhancer.setCallback(new BeanMethodInterceptor<T>(jdbcSession, clazz, id));
            return (T) enhancer.create();
        } else {
            Object bean = BeanUtils.instantiateClass(clazz);
            EntityUtils.setId(bean, id);
            return (T) bean;
        }
    }

    public static <T> List<T> createList(Class<?> one, Class<T> many, Role role, String joinFile, Object bean) {
        if (MetaTableFactory.contains(many)) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(List.class);
            enhancer.setUseCache(true);
            enhancer.setInterceptDuringConstruction(false);
            enhancer.setInterfaces(new Class[]{List.class});
            enhancer.setCallback(new ListMethodInterceptor<T>(jdbcSession, one, many, role, joinFile, bean));
            return (List<T>) enhancer.create();
        } else {
            return null;
        }
    }

    public static void refresh(Object bean) {
        Object id = EntityUtils.getId(bean);
        MetaTable<?> metaTable = MetaTableFactory.get(bean.getClass());
        String sql = metaTable.selectByPrimaryKey();
        Object newBean = jdbcSession.queryForObject(sql, Map.of("id", id), metaTable.getRowMapper());
        BeanUtils.copyProperties(newBean, bean);
    }

    public static <T> T attach(T bean) {
        Object id = EntityUtils.getId(bean);
        MetaTable<T> metaTable = MetaTableFactory.get(bean);
        String sql = metaTable.selectByPrimaryKey();
        return (T) jdbcSession.queryForObject(sql, Map.of("id", id), metaTable.getRowMapper());
    }
}
