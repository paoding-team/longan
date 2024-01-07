package dev.paoding.longan.data.jpa;

import dev.paoding.longan.service.SystemException;
import dev.paoding.longan.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Method;
import java.util.Map;

public class BeanMethodInterceptor<T> implements MethodInterceptor, BeanProxy {
    private final JdbcSession jdbcSession;
    private T bean;
    private final Object id;
    private final String sql;
    private final RowMapper<T> rowMapper;
    private boolean isLoaded;
    private final Class<?> one;
    private final MetaTable<T> metaTable;

    public BeanMethodInterceptor(JdbcSession jdbcSession, Class<T> type, Object id) {
        this.id = id;
        this.metaTable = MetaTableFactory.get(type);
        this.sql = metaTable.selectByPrimaryKey();
        this.rowMapper = metaTable.getRowMapper();
        this.bean = BeanUtils.instantiateClass(type);
        this.one = type;
        EntityUtils.setId(bean, id, metaTable.getPrimaryKey().getType());
        this.jdbcSession = jdbcSession;
    }

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (method.getDeclaringClass() == BeanProxy.class) {
            return getOriginal();
        } else {
            if (!isLoaded && !method.getName().equals("getId")) {
                load();
            }
            return method.invoke(bean, args);
        }
    }

    @Override
    public Object getOriginal() {
        if (!isLoaded) {
            load();
        }
        return bean;
    }

    private void load() {
        try {
            this.bean = jdbcSession.queryForObject(sql, Map.of("id", id), rowMapper);
            EntityUtils.wrap(metaTable, this.bean);
        } catch (EmptyResultDataAccessException e) {
            throw new SystemException(one.getSimpleName() + " with id " + id + " not found.");
        }
        isLoaded = true;
    }
}
