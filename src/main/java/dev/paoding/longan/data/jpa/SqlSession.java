package dev.paoding.longan.data.jpa;

import org.springframework.cglib.beans.BeanMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SqlSession {
    private final JdbcSession jdbcSession;

    public SqlSession(JdbcSession jdbcSession) {
        this.jdbcSession = jdbcSession;
    }

    public int delete(Object entity) {
        Object id = BeanMap.create(entity).get("id");
        return deleteById(entity.getClass(), id);
    }

    public int deleteById(Class<?> type, Object id) {
        MetaTable<?> masterMetaTable = MetaTableFactory.get(type);

        Collection<OneToManyPoint> oneToManyPoints = masterMetaTable.getOneToManyPointMap().values();
        for (OneToManyPoint oneToManyPoint : oneToManyPoints) {
            Class<?> slaverClass = oneToManyPoint.getSlaver();
            MetaTable<?> slaverMetaTable = MetaTableFactory.get(slaverClass);
            String joinColumn = slaverMetaTable.getColumnName(oneToManyPoint.getJoinField());
            if (oneToManyPoint.isOrphanRemoval()) {
                String sql = "select * from " + slaverMetaTable.getName() + " where " + joinColumn + " = ?";
                List<?> slaverObjectList = jdbcSession.query(sql, slaverMetaTable.getRowMapper(), id);
                for (Object salverObject : slaverObjectList) {
                    delete(salverObject);
                }
            } else {
                String sql = "update " + slaverMetaTable.getName() + " set " + joinColumn + " = null where " + joinColumn + " = ?";
                jdbcSession.update(sql, new Object[]{id});
            }
        }

        Collection<ManyToManyPoint> manyToManyPoints = masterMetaTable.getManyToManyPointMap().values();
        for (ManyToManyPoint manyToManyPoint : manyToManyPoints) {
            List<String> tableList = manyToManyPoint.getTableList();
            for (String table : tableList) {
                String sql = "delete from " + table + " where " + masterMetaTable.getAlias() + "_id = ?";
                jdbcSession.update(sql, new Object[]{id});
            }
        }
        return jdbcSession.update(masterMetaTable.deleteByPrimaryKey(), new Object[]{id});
    }

    public int deleteAll(Class<?> type) {
        MetaTable<?> metaTable = MetaTableFactory.get(type);
        String sql = "select * from " + metaTable.getName();
        List<?> beanList = jdbcSession.query(sql, metaTable.getRowMapper());
        for (Object bean : beanList) {
            delete(bean);
        }
        return beanList.size();
    }

    public int deleteById(Class<?> type, List<?> idList) {
        MetaTable<?> metaTable = MetaTableFactory.get(type);
        Map<String, Object> paramMap = Map.of("idList", idList);
        String sql = "select * from " + metaTable.getName() + " where id in (:idList)";
        List<?> beanList = jdbcSession.query(sql, paramMap, metaTable.getRowMapper());
        for (Object bean : beanList) {
            delete(bean);
        }
        return beanList.size();
    }
}
