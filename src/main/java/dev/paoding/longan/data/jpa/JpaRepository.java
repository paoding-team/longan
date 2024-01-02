package dev.paoding.longan.data.jpa;

import dev.paoding.longan.data.Pageable;

import java.util.List;
import java.util.Optional;

public interface JpaRepository<T, ID> {

    long generateId();

    /**
     * 根据 id 查询，如果数据不存在抛出异常
     *
     * @param id id
     * @return
     */
    T get(ID id);

    /**
     * 根据 id 查询
     *
     * @param id
     * @return
     */
    Optional<T> getOptional(ID id);

    /**
     * 根据范例条件查询一个对象
     *
     * @param example
     * @return
     */
    Optional<T> getOptional(Example<T> example);

    List<T> find(Pageable pageable);

    List<T> find(Example<T> example);

    List<T> find(Example<T> example, Pageable pageable);

    List<T> find(List<ID> idList);

    List<T> findAll();

    long count();

    long count(Example<T> example);

    boolean exists(Example<T> example);

    boolean exists(ID id);

    T save(T entity);

    List<T> save(List<T> entityList);

    int deleteById(ID id);

    int deleteAll();

    int delete(List<T> entityList);

    int deleteById(List<ID> idList);

    int increase(ID id, Object... objects);

    /**
     * 只更新不为 null 的值
     *
     * @param entity
     */
    int update(T entity);

    int update(List<T> entityList);

    int update(List<ID> idList, T entity);

    int join(T source, Object target);

    int split(T source, Object target);

    int split(T source, Class<?> type);

    int join(T source, Object target, String role);

    int split(T source, Object target, String role);

    int split(T source, Class<?> type, String role);

    @Deprecated
    List<T> findAll(Pageable pageable);

    @Deprecated
    List<T> findAll(Example<T> example);

    @Deprecated
    List<T> findAll(Example<T> example, Pageable pageable);

    @Deprecated
    List<T> findAllById(List<ID> idList);

    @Deprecated
    Optional<T> findById(ID id);

    @Deprecated
    Optional<T> findOne(Example<T> example);

    @Deprecated
    boolean existsById(ID id);

    @Deprecated
    List<T> saveAll(List<T> entityList);

    @Deprecated
    int delete(T entity);

    @Deprecated
    int deleteAll(List<T> entityList);

    @Deprecated
    int deleteAllById(List<ID> idList);

//    @Deprecated
//    int increase(ID id, String field, Number number);
//
//    @Deprecated
//    int increase(ID id, String field1, Number number1, String field2, Number number2);
//
//    @Deprecated
//    int increase(ID id, String field1, Number number1, String field2, Number number2, String field3, Number
//            number3);

    @Deprecated
    int updateAll(List<T> entityList);

    @Deprecated
    int update(ID id, SqlMap sqlMap);

    int update(ID id, Object... objects);

    @Deprecated
    int update(List<ID> idList, SqlMap sqlMap);

    int update(List<ID> idList, Object... objects);

//    Query<T> query(String sql);

//    <D> Query<D> query(String sql, Class<D> requiredType);

//    List<Map<String, Object>> queryForList(String sql, Map<String, ?> paramMap);

//    <D> List<D> queryForList(String sql, Map<String, ?> paramMap, Class<D> elementType);

//    Map<String, Object> queryForMap(String sql, Map<String, ?> paramMap);

//    <D> D queryForObject(String sql, Map<String, ?> paramMap, Class<D> requiredType);
}
