package dev.paoding.longan.data.jpa;

import com.google.common.collect.ImmutableMap;
import dev.paoding.longan.util.EntityUtils;
import dev.paoding.longan.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class ListMethodInterceptor<T> implements MethodInterceptor {
    private final Logger logger = LoggerFactory.getLogger(ListMethodInterceptor.class);
    private final JdbcSession jdbcSession;
    private boolean isLoaded;
    private Class<?> many;
    private List<T> list;
    private Object bean;
    private Role role;
    private String joinFile;
    private MetaTable<T> metaTable;

    public ListMethodInterceptor(JdbcSession jdbcSession, Class<?> one, Class<T> many, Role role, String joinFile, Object bean) {
        this.metaTable = MetaTableFactory.get(many);
        this.many = many;
        this.bean = bean;
        this.role = role;
        this.joinFile = joinFile;
        if (joinFile.isEmpty()) {
            this.joinFile = StringUtils.lowerFirst(one.getSimpleName());
        }
        this.jdbcSession = jdbcSession;
    }

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (!isLoaded) {
            load();
        }
        return method.invoke(list, args);
    }


    private void load() {
        if (role == null) {
            Map<String, Object> map = ImmutableMap.of(joinFile, bean);
//            MethodParser methodParser = MethodParser.of("select", many, StringUtils.upperFirst(joinFile), map, role);
            MethodParser methodParser = MethodParser.of("select", many, StringUtils.upperFirst(joinFile), map);
            String sql = methodParser.getSql();
            this.list = jdbcSession.query(sql, methodParser.getParamMap(), metaTable.getRowMapper());
        } else {
            Map<String, Object> map = ImmutableMap.of(role.getName(), bean);
//            MethodParser methodParser = MethodParser.of("select", many, StringUtils.upperFirst(role.getName()), map, role);
            MethodParser methodParser = MethodParser.of("select", many, StringUtils.upperFirst(role.getName()), map);
            String sql = methodParser.getSql();
            this.list = jdbcSession.query(sql, methodParser.getParamMap(), metaTable.getRowMapper());
        }
        isLoaded = true;
        EntityUtils.wrap(metaTable, list);
    }
}
