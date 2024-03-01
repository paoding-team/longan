package dev.paoding.longan.data.jpa;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonSyntaxException;
import dev.paoding.longan.annotation.I18n;
import dev.paoding.longan.core.Internationalization;
import dev.paoding.longan.data.Entity;
import dev.paoding.longan.data.Pageable;
import dev.paoding.longan.data.Snowflake;
import dev.paoding.longan.service.SystemException;
import dev.paoding.longan.util.EntityUtils;
import dev.paoding.longan.util.GsonUtils;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.*;

public class JpaRepositoryProxy<T, ID> implements InvocationHandler, JpaRepository<T, ID> {
    private final Method[] methods;
    private final MetaTable<T> metaTable;
    private String database;
    private JdbcSession jdbcSession;
    private final Class<T> clazz;
    private final Snowflake snowflake = new Snowflake();
    private final DynamicSqlParser dynamicSqlParser = new DynamicSqlParser();

    {
        methods = JpaRepository.class.getMethods();
    }

    public JpaRepositoryProxy(Class<T> clazz) {
        this.metaTable = MetaTableFactory.get(clazz);
        this.clazz = clazz;
    }

    public void setDatabase(String database) {
        this.database = database;
    }


    public void setJdbcSession(JdbcSession jdbcSession) {
        this.jdbcSession = jdbcSession;
    }

    @Override
    public long generateId() {
        return snowflake.nextId();
    }

    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        if (method.isDefault()) {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
            constructor.setAccessible(true);

            Class<?> declaringClass = method.getDeclaringClass();
            int allModes = MethodHandles.Lookup.PUBLIC | MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED | MethodHandles.Lookup.PACKAGE;

            return constructor.newInstance(declaringClass, allModes)
                    .unreflectSpecial(method, declaringClass)
                    .bindTo(proxy)
                    .invokeWithArguments(args);
        }
        String methodName = method.getName();
        for (Method innerMethod : methods) {
            if (method.equals(innerMethod)) {
                return method.invoke(this, args);
            }
        }


        Class<?> returnType = method.getReturnType();

        if (method.isAnnotationPresent(Query.class)) {
            Map<String, Object> paramMap = SqlParser.toMap(method, args);
            Query query = method.getAnnotation(Query.class);
            String sql = query.value().trim();
            if (query.dynamic()) {
//                sql = SqlParser.parseDynamicSql(sql, paramMap);
                sql = dynamicSqlParser.parse(sql, paramMap);
//            } else {
//                sql = SqlParser.parseStaticSql(sql, paramMap);
            }
            if (paramMap.containsKey("pageable")) {
                Pageable pageable = (Pageable) paramMap.get("pageable");
                if (pageable != null) {
                    sql += pageable.toSql();
                }
            }
            if (returnType.isAssignableFrom(List.class)) {
                return EntityUtils.wrap(metaTable, jdbcSession.query(sql, paramMap, metaTable.getRowMapper()));
            } else if (returnType.isPrimitive()) {
                return jdbcSession.queryForObject(sql, paramMap, returnType);
            } else {
                return EntityUtils.wrap(metaTable, jdbcSession.queryForObject(sql, paramMap, metaTable.getRowMapper()));
            }
        }

        if (method.isAnnotationPresent(Update.class)) {
            Map<String, Object> paramMap = SqlParser.toMap(method, args);
            Update query = method.getAnnotation(Update.class);
            String sql = query.value().trim();
            return jdbcSession.update(sql, paramMap);
        }

        Parameter[] parameters = method.getParameters();
        Map<String, Object> paramMap = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            Object object = args[i];
            if (object != null) {
                paramMap.put(parameters[i].getName(), object);
            }
        }

        if (methodName.startsWith("findBy")) {
            return select(methodName.substring(6), paramMap, returnType, 0, false);
        } else if (methodName.startsWith("findDistinctBy")) {
            return select(methodName.substring(14), paramMap, returnType, 0, true);
        } else if (methodName.startsWith("findDistinctTop")) {
            int i = methodName.indexOf("By");
            int limit = 0;
            if (i > 15) {
                limit = Integer.parseInt(methodName.substring(15, i));
            }
            paramMap.remove("pageable");
            return select(methodName.substring(i + 2), paramMap, returnType, limit, true);
        } else if (methodName.startsWith("findTop")) {
            int i = methodName.indexOf("By");
            int limit = 0;
            if (i > 7) {
                limit = Integer.parseInt(methodName.substring(7, i));
            }
            paramMap.remove("pageable");
            return select(methodName.substring(i + 2), paramMap, returnType, limit, false);
        } else if (methodName.startsWith("countBy")) {
            MethodParser methodParser = MethodParser.of("count", clazz, methodName.substring(7), paramMap);
            String sql = methodParser.getSql();
            return jdbcSession.queryForObject(sql, methodParser.getParamMap(), returnType);
        } else if (methodName.startsWith("existsBy")) {
            MethodParser methodParser = MethodParser.of("count", clazz, methodName.substring(8), paramMap);
            String sql = methodParser.getSql();
            return jdbcSession.queryForObject(sql, methodParser.getParamMap(), Long.class) > 0;
        } else if (methodName.startsWith("deleteBy")) {
            MethodParser methodParser = MethodParser.of("delete", clazz, methodName.substring(8), paramMap);
            String sql = methodParser.getSql();
            return jdbcSession.update(sql, methodParser.getParamMap());
        }

        switch (methodName) {
            case "toString":
                return proxy.getClass().getInterfaces()[0].getName();
            case "hashCode":
                return proxy.getClass().hashCode();
            case "equals":
                assert args[0] != null;
                return proxy.getClass().equals(args[0].getClass());
        }

        return proxy.getClass().toGenericString();
    }

    private Object select(String methodName, Map<String, Object> paramMap, Class<?> returnType, int limit, boolean distinct) {
        MethodParser methodParser = MethodParser.of("select", clazz, methodName, paramMap, distinct);
        String sql = methodParser.getSql();
        if (limit > 0) {
            sql += " limit " + limit;
        }
        if (returnType.isAssignableFrom(List.class)) {
            return EntityUtils.wrap(metaTable, jdbcSession.query(sql, methodParser.getParamMap(), metaTable.getRowMapper()));
        } else if (Optional.class.isAssignableFrom(returnType)) {
            try {
                return Optional.ofNullable(EntityUtils.wrap(metaTable, jdbcSession.queryForObject(sql, methodParser.getParamMap(), metaTable.getRowMapper())));
            } catch (EmptyResultDataAccessException e) {
                return Optional.empty();
            }
        } else {
            try {
                return EntityUtils.wrap(metaTable, jdbcSession.queryForObject(sql, methodParser.getParamMap(), metaTable.getRowMapper()));
            } catch (EmptyResultDataAccessException e) {
                return null;
            }
        }
    }


    @Override
    public T get(ID id) {
        String sql = metaTable.selectByPrimaryKey();
        Map<String, ID> paramMap = Map.of("id", id);
        return EntityUtils.wrap(metaTable, jdbcSession.queryForObject(sql, paramMap, metaTable.getRowMapper()));
    }

    @Override
    public Optional<T> getOptional(ID id) {
        try {
            return Optional.ofNullable(get(id));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<T> getOptional(Example<T> example) {
        MatchResult matchResult = example.match();
        String sql = "select * from " + metaTable.getName() + (matchResult.getWhere().isEmpty() ? "" : " where " + matchResult.getWhere());
        try {
            T entity = jdbcSession.queryForObject(sql, matchResult.getParamMap(), metaTable.getRowMapper());
            return Optional.ofNullable(EntityUtils.wrap(metaTable, entity));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        return getOptional(id);
    }

    @Override
    public Optional<T> findOne(Example<T> example) {
        return getOptional(example);
    }

    @Override
    public boolean exists(ID id) {
        String sql = "select count(*) from " + metaTable.getName() + " where " + metaTable.getPrimaryKey().getName() + " = :id";
        Map<String, Object> paramMap = ImmutableMap.of("id", id);
        return jdbcSession.queryForLong(sql, paramMap) > 0;
    }

    @Override
    public boolean existsById(ID id) {
        return exists(id);
    }

    @Override
    public boolean exists(Example<T> example) {
        return count(example) > 0;
    }

    @Override
    public List<T> find(Pageable pageable) {
        String sql = "select * from " + metaTable.getName();
        if (pageable != null) {
            sql += pageable.toSql();
        }
        return EntityUtils.wrap(metaTable, jdbcSession.query(sql, metaTable.getRowMapper()));
    }

    @Override
    public List<T> find(Example<T> example) {
        MatchResult matchResult = example.match();
        String sql = "select * from " + metaTable.getName() + (matchResult.getWhere().isEmpty() ? "" : " where " + matchResult.getWhere());
        return EntityUtils.wrap(metaTable, jdbcSession.query(sql, matchResult.getParamMap(), metaTable.getRowMapper()));
    }

    @Override
    public List<T> find(Example<T> example, Pageable pageable) {
        MatchResult matchResult = example.match();
        String sql = "select * from " + metaTable.getName() + (matchResult.getWhere().isEmpty() ? "" : " where " + matchResult.getWhere());
        if (pageable != null) {
            sql += pageable.toSql();
        }
        return EntityUtils.wrap(metaTable, jdbcSession.query(sql, matchResult.getParamMap(), metaTable.getRowMapper()));
    }

    @Override
    public List<T> find(List<ID> idList) {
        if (idList == null || idList.size() == 0) {
            return new ArrayList<>();
        }
        String sql = "select * from " + metaTable.getName() + " where " + metaTable.getPrimaryKey().getName() + " in (:idList)";
        Map<String, Object> paramMap = ImmutableMap.of("idList", idList);
        return EntityUtils.wrap(metaTable, jdbcSession.query(sql, paramMap, metaTable.getRowMapper()));
    }

    @Override
    public List<T> findAll(Example<T> example) {
        return find(example);
    }

    @Override
    public List<T> findAll(Example<T> example, Pageable pageable) {
        return find(example, pageable);
    }

    @Override
    public List<T> findAllById(List<ID> idList) {
        return find(idList);
    }


    @Override
    public List<T> findAll() {
        String sql = "select * from " + metaTable.getName() + " order by " + metaTable.getPrimaryKey().getName() + " desc";
        return EntityUtils.wrap(metaTable, jdbcSession.query(sql, metaTable.getRowMapper()));
    }

    @Override
    public List<T> findAll(Pageable pageable) {
        return find(pageable);
    }

    @Override
    public long count() throws DataAccessException {
        String sql = "select count(1) from " + metaTable.getName();
        return jdbcSession.queryForLong(sql);
    }

    @Override
    public long count(Example<T> example) {
        MatchResult matchResult = example.match();
        String sql = "select count(1) from " + metaTable.getName() + (matchResult.getWhere().isEmpty() ? "" : " where " + matchResult.getWhere());
        return jdbcSession.queryForLong(sql, matchResult.getParamMap());
    }

    @Override
    public T save(T entity) {
        if (entity == null) {
            throw new RuntimeException("object must not be null");
        }
        if (Internationalization.isEnabled() && metaTable.isInternationalized()) {
            serialize(entity);
        }
        MetaColumn primaryKey = metaTable.getPrimaryKey();
        if (primaryKey.getGenerator().equals(Generator.SNOWFLAKE)) {
            EntityUtils.setId(entity, snowflake.nextId(), primaryKey.getType());
            String sql = metaTable.insert(database);
            jdbcSession.update(sql, new BeanPropertySqlParameterSource(entity));
        } else if (primaryKey.getGenerator().equals(Generator.AUTO)) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            String sql = metaTable.insert(database);
            jdbcSession.update(sql, new BeanPropertySqlParameterSource(entity), keyHolder);
            Number key = keyHolder.getKey();
            String keyName = primaryKey.getAlias();
            Class<?> keyType = primaryKey.getType();
            if (keyType == long.class || keyType == Long.class) {
                BeanMap.create(entity).put(keyName, key.longValue());
            } else if (keyType == int.class || keyType == Integer.class) {
                BeanMap.create(entity).put(keyName, key.intValue());
            } else if (keyType == short.class || keyType == Short.class) {
                BeanMap.create(entity).put(keyName, key.shortValue());
            }
        } else {
            String sql = metaTable.insert(database);
            jdbcSession.update(sql, new BeanPropertySqlParameterSource(entity));
        }

        return entity;
    }

    private <T> void serialize(T entity) {
        Field[] fieldArray = entity.getClass().getDeclaredFields();
        try {
            for (Field field : fieldArray) {
                if (field.isAnnotationPresent(I18n.class)) {
                    field.setAccessible(true);
                    Object value = field.get(entity);
                    if (value != null) {
                        Map<String, String> map = Map.of(Internationalization.getLanguage(), value.toString());
                        field.set(entity, GsonUtils.toJson(map));
                    }
                }
            }
        } catch (IllegalAccessException | JsonSyntaxException e) {
            throw new SystemException(e);
        }
    }

    @Override
    public List<T> save(List<T> entityList) {
        for (T entity : entityList) {
            save(entity);
        }
        return entityList;
    }

    @Override
    public List<T> saveAll(List<T> entityList) {
        return save(entityList);
    }

    @Override
    public int increase(ID id, Object... objects) {
        if (objects.length == 0) return 0;
        if (objects.length % 2 != 0) {
            throw new SystemException("The length of objects must be even number.");
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", id);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < objects.length; i = i + 2) {
            String columnName = metaTable.getColumnName(objects[i].toString());
            stringBuilder.append(columnName).append(" = ").append(columnName).append(" + :").append(columnName).append(", ");
            paramMap.put(columnName, objects[i + 1]);
        }
        String sql = "update " + metaTable.getName() + " set " + stringBuilder.substring(0, stringBuilder.length() - 2) + " where " + metaTable.getPrimaryKey().getName() + " = :id";
        return jdbcSession.update(sql, paramMap);
    }
//
//    @Override
//    public int increase(ID id, String field, Number number) {
//        return increase(id, field, number);
//    }
//
//    @Override
//    public int increase(ID id, String field1, Number number1, String field2, Number number2) {
//        return increase(id, field1, number1, field2, number2);
//    }
//
//    @Override
//    public int increase(ID id, String field1, Number number1, String field2, Number number2, String field3, Number number3) {
//        return increase(id, field1, number1, field2, number2, field3, number3);
//    }

    protected Object convert(Object value) {
        if (value != null) {
            Class<?> type = value.getClass();
            if (type.isAnnotationPresent(Entity.class)) {
                return EntityUtils.getId(value);
            }
            if (Enum.class.isAssignableFrom(type)) {
                return value.toString();
            }
            if (type.isArray()) {
                return Database.createArrayOf(value);
            }
        }
        return value;
    }

    @Override
    public int update(ID id, SqlMap sqlMap) {
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, Object> originalMap = sqlMap.build();
        for (String key : originalMap.keySet()) {
            String name = metaTable.getColumnName(key);
            paramMap.put(name, convert(originalMap.get(key)));
        }
        originalMap.clear();

        StringBuilder stringBuilder = new StringBuilder();
        for (String key : paramMap.keySet()) {
            stringBuilder.append(key).append(" = :").append(key).append(", ");
        }
        String sql = "update " + metaTable.getName() + " set " + stringBuilder.substring(0, stringBuilder.length() - 2) + " where " + metaTable.getPrimaryKey().getName() + " = :id";
        paramMap.put("id", id);
        return jdbcSession.update(sql, paramMap);
    }

    @Override
    public int update(List<ID> idList, SqlMap sqlMap) {
        Map<String, Object> paramMap = new HashMap<>();
        Map<String, Object> originalMap = sqlMap.build();
        for (String key : originalMap.keySet()) {
            String name = metaTable.getColumnName(key);
            paramMap.put(name, convert(originalMap.get(key)));
        }
        originalMap.clear();

        StringBuilder stringBuilder = new StringBuilder();
        for (String key : paramMap.keySet()) {
            stringBuilder.append(key).append(" = :").append(key).append(", ");
        }
        String sql = "update " + metaTable.getName() + " set " + stringBuilder.substring(0, stringBuilder.length() - 2) + " where " + metaTable.getPrimaryKey().getName() + " in (:idList)";
        paramMap.put("idList", idList);
        return jdbcSession.update(sql, paramMap);
    }

    @Override
    public int update(ID id, Object... objects) {
        if (objects.length == 0) return 0;
        if (objects.length % 2 != 0) {
            throw new SystemException("The length of objects must be even");
        }

        SqlMap sqlMap = SqlMap.of();
        for (int i = 0; i < objects.length; i = i + 2) {
            sqlMap.put(objects[i].toString(), objects[i + 1]);
        }
        return update(id, sqlMap);
    }

    @Override
    public int update(List<ID> idList, Object... objects) {
        if (objects.length == 0) return 0;
        if (objects.length % 2 != 0) {
            throw new SystemException("The length of objects must be even");
        }

        SqlMap sqlMap = SqlMap.of();
        for (int i = 0; i < objects.length; i = i + 2) {
            sqlMap.put(objects[i].toString(), objects[i + 1]);
        }
        return update(idList, sqlMap);
    }

    @Override
    public int update(T entity) {
        if (entity == null) {
            throw new RuntimeException("object must not be null");
        }
        if (Internationalization.isEnabled() && metaTable.isInternationalized()) {
            merge(entity);
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", metaTable.getPrimaryKey().getValue(entity));
        metaTable.getMetaColumnList().forEach(metaColumn -> {
            Object value = metaColumn.getValue(entity);
            if (value != null) {
                paramMap.put(metaColumn.getName(), convert(value));
            }
        });

        if (paramMap.size() < 2 || !paramMap.containsKey("id")) {
            return 0;
        }

        StringBuilder stringBuilder = new StringBuilder();
        paramMap.keySet().stream().filter(key -> !key.equals("id")).forEach(key -> stringBuilder.append(key).append(" = :")
                .append(key).append(", "));
        String sql = "update " + metaTable.getName() + " set " + stringBuilder.substring(0, stringBuilder.length() - 2) + " where " + metaTable.getPrimaryKey().getName() + " = :id";
        return jdbcSession.update(sql, paramMap);
    }

    private void merge(T entity) {
        T old = BeanFactory.attach(entity);
        Field[] fieldArray = entity.getClass().getDeclaredFields();
        try {
            for (Field field : fieldArray) {
                if (field.isAnnotationPresent(I18n.class)) {
                    field.setAccessible(true);
                    Object newValue = field.get(entity);
                    if (newValue != null) {
                        Object oldValue = field.get(old);
                        if (oldValue == null) {
                            field.set(entity, Map.of(Internationalization.getLanguage(), newValue.toString()));
                        } else {
                            Map<String, String> map = GsonUtils.toLocaleMap(oldValue.toString());
                            map.put(Internationalization.getLanguage(), newValue.toString());
                            field.set(entity, GsonUtils.toJson(map));
                        }
                    }
                }
            }
        } catch (IllegalAccessException | JsonSyntaxException e) {
            throw new SystemException(e);
        }
    }


    @Override
    public int update(List<T> entityList) {
        return entityList.stream().mapToInt(this::update).sum();
    }

    @Override
    public int update(List<ID> idList, T entity) {
        return 0;
    }

    @Override
    public int updateAll(List<T> entityList) {
        return update(entityList);
    }

    @Override
    public int delete(T entity) {
        ID id = (ID) BeanMap.create(entity).get("id");
        return deleteById(id);
    }

    @Override
    public int delete(List<T> entityList) {
        if (entityList == null || entityList.size() == 0) {
            return 0;
        }
        List<ID> idList = new ArrayList<>();
        for (T entity : entityList) {
            ID id = (ID) BeanMap.create(entity).get("id");
            idList.add(id);
        }
        return deleteById(idList);
    }

    @Override
    public int deleteById(List<ID> idList) {
        if (idList == null || idList.size() == 0) {
            return 0;
        }
        SqlSession sqlSession = new SqlSession(jdbcSession);
        return sqlSession.deleteById(clazz, idList);
    }


    @Override
    public int deleteById(ID id) {
        SqlSession sqlSession = new SqlSession(jdbcSession);
        return sqlSession.deleteById(metaTable.getType(), id);
    }

    @Override
    public int deleteAll() {
        SqlSession sqlSession = new SqlSession(jdbcSession);
        return sqlSession.deleteAll(clazz);
    }

    @Override
    public int deleteAll(List<T> entityList) {
        return delete(entityList);
    }

    @Override
    public int deleteAllById(List<ID> idList) {
        return deleteById(idList);
    }

    @Override
    public int join(T start, Object end) {
        return join(start, end, "");
    }

    @Override
    public int split(T start, Object end) {
        return split(start, end, "");
    }

    @Override
    public int split(T start, Class<?> type) {
        return split(start, type, "");
    }

    @Override
    public int join(T start, Object end, String role) {
        return joinOrSplit(start, end, role, true);
    }

    @Override
    public int split(T start, Object end, String role) {
        return joinOrSplit(start, end, role, false);
    }

    @Override
    public int split(T start, Class<?> type, String role) {
        String startName = SqlParser.toDatabaseName(start.getClass().getSimpleName());
        String endName = SqlParser.toDatabaseName(type.getSimpleName());
        Object startId = BeanMap.create(start).get("id");

        Map<String, Object> paraMap = ImmutableMap.of(startName + "_id", startId);
        String sql;
        sql = SqlParser.toSplitSqlAll(startName, endName, role);
        return jdbcSession.update(sql, paraMap);
    }

    private int joinOrSplit(T start, Object end, String role, boolean join) {
        String startName = SqlParser.toDatabaseName(start.getClass().getSimpleName());
        String endName = SqlParser.toDatabaseName(end.getClass().getSimpleName());
        Object startId = BeanMap.create(start).get("id");
        Object endId = BeanMap.create(end).get("id");
        Map<String, Object> paraMap = ImmutableMap.of(startName + "_id", startId, endName + "_id", endId);
        String sql;
        if (join) {
            sql = SqlParser.toCountSql(startName, endName, role);
            if (jdbcSession.queryForLong(sql, paraMap) == 0) {
                sql = SqlParser.toJoinSql(startName, endName, role);
                return jdbcSession.update(sql, paraMap);
            }
        } else {
            sql = SqlParser.toSplitSql(startName, endName, role);
            return jdbcSession.update(sql, paraMap);
        }
        return 0;
    }

    @Override
    public boolean exists(T source, Object target) {
        return exists(source, target, "");
    }

    @Override
    public boolean exists(T source, Object target, String role) {
        String startName = SqlParser.toDatabaseName(source.getClass().getSimpleName());
        String endName = SqlParser.toDatabaseName(target.getClass().getSimpleName());
        Object startId = BeanMap.create(source).get("id");
        Object endId = BeanMap.create(target).get("id");
        Map<String, Object> paraMap = ImmutableMap.of(startName + "_id", startId, endName + "_id", endId);
        String sql = SqlParser.toCountSql(startName, endName, role);
        return jdbcSession.queryForLong(sql, paraMap) > 0;
    }

    //    @Override
//    public int delete(String sql, Map<String, ?> paramMap) throws DataAccessException {
//        return jdbcTemplate.update(sql, new MapSqlParameterSource(paramMap));
//    }
//
//    @Override
//    public int delete(String sql, String k1, Object v1) throws DataAccessException {
//        Map<String, Object> paramMap = ImmutableMap.of(k1, v1);
//        return jdbcTemplate.update(sql, new MapSqlParameterSource(paramMap));
//    }


//    @Override
//    public int update(String sql, Map<String, ?> paramMap) throws DataAccessException {
//        return jdbcTemplate.update(sql, new MapSqlParameterSource(paramMap));
//    }

//    @Override
//    public Query<T> query(String sql) {
//        return new Query<>(jdbcTemplate, metaTable, sql);
//    }

//    @Override
//    public <D> Query<D> query(String sql, Class<D> requiredType) {
//        return new Query<>(jdbcTemplate, BeanPropertyRowMapper.newInstance(requiredType), sql);
//    }

//    @Override
//    public List<Map<String, Object>> queryForList(String sql, Map<String, ?> paramMap)
//            throws DataAccessException {
//
//        return jdbcTemplate.queryForList(sql, new MapSqlParameterSource(paramMap));
//    }

//    @Override
//    public <D> List<D> queryForList(String sql, Map<String, ?> paramMap, Class<D> elementType)
//            throws DataAccessException {
//
//        return jdbcTemplate.queryForList(sql, new MapSqlParameterSource(paramMap), elementType);
//    }

//    @Override
//    public Map<String, Object> queryForMap(String sql, Map<String, ?> paramMap) throws DataAccessException {
//        Map<String, Object> result = jdbcTemplate.queryForObject(sql, paramMap, new ColumnMapRowMapper());
//        Assert.state(result != null, "No result map");
//        return result;
//    }

//    @Override
//    @Nullable
//    public <D> D queryForObject(String sql, Map<String, ?> paramMap, Class<D> requiredType)
//            throws DataAccessException {
//
//        return jdbcTemplate.queryForObject(sql, paramMap, new SingleColumnRowMapper<>(requiredType));
//    }
}
