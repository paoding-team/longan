package dev.paoding.longan.data.jpa;

import com.google.common.base.Joiner;
import dev.paoding.longan.core.ClassPathBeanScanner;
import dev.paoding.longan.data.Entity;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
public class TableMetaDataManager {
    private final JdbcSession jdbcSession;
    private Connection connection;


    public TableMetaDataManager(JdbcSession jdbcSession) {
        this.jdbcSession = jdbcSession;
    }

    public void populate() {
        try (Connection connection = jdbcSession.getConnection()) {
            this.connection = connection;
            java.sql.DatabaseMetaData databaseMetaData = connection.getMetaData();
            populate(databaseMetaData);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void populate(java.sql.DatabaseMetaData databaseMetaData) {
        List<Class<?>> entityList = ClassPathBeanScanner.getModuleEntityClassList();
        for (Class<?> classType : entityList) {
            Entity entity = classType.getAnnotation(Entity.class);
            if (entity.virtual()) {
                continue;
            }
            Field[] fields = classType.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(ManyToMany.class)) {
                    ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
                    Type fieldType = field.getGenericType();
                    if (fieldType instanceof ParameterizedType) {
                        Class<?> type = (Class<?>) ((ParameterizedType) fieldType).getActualTypeArguments()[0];
                        String source = SqlParser.toDatabaseName(classType.getSimpleName());
                        String target = SqlParser.toDatabaseName(type.getSimpleName());
                        createMappingTable(databaseMetaData, source, target, manyToMany.role());
                    }
                }
            }
            MetaTable<?> metaTable = MetaTableFactory.get(classType);
            String tableName = metaTable.getName();

            Map<String, MetaIndex> indexMap = getIndexMap(databaseMetaData, tableName);
            Map<String, MetaColumn> columnMap = getColumnMap(databaseMetaData, tableName);
            if (columnMap.isEmpty()) {
                List<String> sqlList = metaTable.generateCreateSql();
                for (String sql : sqlList) {
                    execute(sql);
                }
            } else {
                List<MetaColumn> metaColumnList = metaTable.getMetaColumnList();
                for (MetaColumn metaColumn : metaColumnList) {
                    String columnName = metaColumn.getName();
                    if (columnMap.containsKey(columnName)) {
                        if (!metaColumn.isNullable() && columnMap.get(columnName).isNullable()) {
                            execute("ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " SET NOT NULL;");
                        }
                        if (metaColumn.isNullable() && !columnMap.get(columnName).isNullable()) {
                            execute("ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " DROP NOT NULL;");
                        }

                        String indexName = "idx_" + columnName;
                        if (metaColumn.isUnique()) {
                            if (indexMap.containsKey(indexName)) {
                                execute("DROP INDEX IF EXISTS " + indexName);
                            }
                            if (!indexMap.containsKey(indexName)) {
                                createIndex(indexName, tableName, columnName, true);
                            }
                        }
                    } else {
                        execute("ALTER TABLE " + tableName + " ADD " + metaColumn.generateText());
                        if (metaColumn.isUnique()) {
                            createUniqueIndex(tableName, columnName);
                        }
                    }
                }
            }

            if (classType.isAnnotationPresent(Table.class)) {
                Table table = classType.getAnnotation(Table.class);
                Index[] indexes = table.indexes();
                for (Index index : indexes) {
                    String[] fieldNames = index.columnNames();
                    List<String> columnNameList = new ArrayList<>();
                    if (fieldNames.length > 0) {
                        for (String fieldName : fieldNames) {
                            String columnName = metaTable.getColumnName(fieldName);
                            if (columnName == null) {
                                throw new RuntimeException("Failed to create index, not found " + fieldName + " field on " + classType.getSimpleName() + " entity.");
                            }
                            columnNameList.add(columnName);
                        }

                        String columnNames = Joiner.on(", ").join(columnNameList);
                        String indexName = index.name();
                        if (index.name().isEmpty()) {
                            indexName = "idx_" + Joiner.on("_").join(columnNameList);
                        }
                        if (index.unique()) {
                            if (indexMap.containsKey(indexName)) {
                                execute("DROP INDEX IF EXISTS " + indexName);
                            }
                            if (!indexMap.containsKey(indexName)) {
                                createIndex(indexName, tableName, columnNames, true);
                            }
                        } else {
                            if (!indexMap.containsKey(indexName)) {
                                createIndex(indexName, tableName, columnNames, false);
                            }
                            if (indexMap.containsKey(indexName)) {
                                execute("DROP INDEX IF EXISTS " + indexName);
                            }
                        }
                    }
                }
            }
        }
    }

    private void createUniqueIndex(String tableName, String columnName) {
        String indexName = "uk_" + columnName;
        execute("CREATE UNIQUE INDEX " + indexName + " ON " + tableName + " (" + columnName + ")");
    }

    private void createIndex(String indexName, String tableName, String columnName, boolean unique) {
        execute("CREATE" + (unique ? " UNIQUE" : "") + " INDEX " + indexName + " ON " + tableName + " (" + columnName + ")");
    }

    private void createMappingTable(java.sql.DatabaseMetaData databaseMetaData, String source, String target, String role) {
        if (!role.isEmpty()) {
            role = "_" + role;
        }
        String table;
        String sourceId;
        String targetId;
        if (source.compareTo(target) < 0) {
            table = source + "_" + target + role + "_link";
            sourceId = source + "_id";
            targetId = target + "_id";
        } else {
            table = target + "_" + source + role + "_link";
            sourceId = target + "_id";
            targetId = source + "_id";
        }

        Map<String, MetaColumn> columnMap = getColumnMap(databaseMetaData, table);
        if (columnMap.isEmpty()) {
            String createTableSql = "\nCREATE TABLE " + table + " (\n\t" +
                    sourceId + " BIGINT,\n\t" +
                    targetId + " BIGINT,\n\t" +
                    "CONSTRAINT pk_" + table +
                    "\n\t\tPRIMARY KEY (" + sourceId + ", " + targetId + ")\n)";
            execute(createTableSql);

            execute("CREATE INDEX idx_" + table + "_" + sourceId + " ON " + table + " (" + sourceId + ")");
            execute("CREATE INDEX idx_" + table + "_" + targetId + " ON " + table + " (" + targetId + ")");
        }
    }


    private void execute(String sql) {
        SqlLogger.log(sql);
        try {
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    private Map<String, MetaColumn> getColumnMap(java.sql.DatabaseMetaData databaseMetaData, String table) {
        Map<String, MetaColumn> columnMap = new HashMap<>();
        try {
            ResultSet resultSet = databaseMetaData.getColumns(null, "public", table, null);
            while (resultSet.next()) {
                MetaColumn metaColumn = new MetaColumn();
                metaColumn.setName(resultSet.getString("COLUMN_NAME"));
                if (Database.isPostgresql()) {
                    metaColumn.setNullable(resultSet.getBoolean("IS_NULLABLE"));
                } else {
                    metaColumn.setNullable(resultSet.getString("IS_NULLABLE").equals("YES"));
                }

                columnMap.put(metaColumn.getName(), metaColumn);
            }
            resultSet.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return columnMap;
    }

    private Map<String, MetaIndex> getIndexMap(java.sql.DatabaseMetaData databaseMetaData, String table) {
        Map<String, MetaIndex> indexMap = new HashMap<>();
        try {
            ResultSet resultSet = databaseMetaData.getIndexInfo(null, "public", table, false, false);
            while (resultSet.next()) {
                String indexName = resultSet.getString("index_name");
                boolean unique = !resultSet.getBoolean("non_unique");
                MetaIndex metaIndex = new MetaIndex();
                metaIndex.setName(indexName);
                metaIndex.setUnique(unique);
                indexMap.put(indexName, metaIndex);

            }
            resultSet.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return indexMap;
    }

}
