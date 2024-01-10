package dev.paoding.longan.data.jpa;

import dev.paoding.longan.annotation.*;
import dev.paoding.longan.data.Entity;
import dev.paoding.longan.data.Transient;
import dev.paoding.longan.util.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class MetaTable<T> {
    /**
     * table name, name of @Table or alias of Entity.
     */
    private String name;
    /**
     * Entity class name, for example: alias of GoodsOrder.class is goods_order.
     */
    private String alias;
    private boolean internationalized;
    private MetaColumn primaryKey;
    private final List<MetaColumn> metaColumnList = new ArrayList<>();
    private final List<MetaIndex> metaIndexList = new ArrayList<>();
    private final Map<String, String> nameMap = new HashMap<>();
    private final Map<String, Class<?>> fieldTypeMap = new HashMap<>();
    private final List<Field> oneToManyFieldList = new ArrayList<>();
    private final List<Field> manyToManyFieldList = new ArrayList<>();
    private final List<Field> accessFieldList = new ArrayList<>();
    private final Map<String, OneToManyPoint> oneToManyPointMap = new HashMap<>();
    private final Map<String, ManyToManyPoint> manyToManyPointMap = new HashMap<>();
    private String select, insert, update, delete;
    private String selectByPrimaryKey, updateByPrimaryKey, deleteByPrimaryKey;
    private RowMapper<T> rowMapper;
    private Class<T> type;

    public MetaTable(Class<T> clazz) {
        init(clazz);
    }

//    public void addOneToMany(String joinField,Class master,Class slaver){
//        OneToManyPoint oneToManyPoint = new OneToManyPoint(master, slaver);
//        oneToManyPoint.setJoinField(joinField);
//        oneToManyPoint.setOrphanRemoval(false);
//        oneToManyPointMap.put(StringUtils.lowerFirst(slaver.getSimpleName()), oneToManyPoint);
//    }
//
//    public void addManyToMany(String name, String field, ManyToManyPoint manyToManyPoint, Class slaver) {
//        if (!manyToManyPointMap.containsKey(name)) {
//            manyToManyPointMap.put(name, manyToManyPoint);
//            fieldTypeMap.put(name, slaver);
//        }
//    }

    private void init(Class<T> clazz) {
        this.type = clazz;
        this.rowMapper = BeanPropertyRowMapper.newInstance(clazz);
        if (clazz.isAnnotationPresent(Table.class)) {
            String tableName = clazz.getAnnotation(Table.class).name();
            if (tableName.isEmpty()) {
                tableName = clazz.getSimpleName();
            }
            this.setName(tableName);
        } else {
            this.setName(clazz.getSimpleName());
        }
        this.alias = SqlParser.toDatabaseName(clazz.getSimpleName());

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (field.getModifiers() != Modifier.PRIVATE && field.getModifiers() != Modifier.PUBLIC && field.getModifiers() != Modifier.PROTECTED) {
                continue;
            }
            if (field.isAnnotationPresent(Transient.class)) {
                continue;
            }
            if (Collection.class.isAssignableFrom(field.getType())) {
                if (field.isAnnotationPresent(ManyToMany.class)) {
                    Type fieldType = field.getGenericType();
                    if (fieldType instanceof ParameterizedType) {
                        Class<?> slaver = (Class<?>) ((ParameterizedType) fieldType).getActualTypeArguments()[0];
                        String role = field.getAnnotation(ManyToMany.class).role();
                        String name = StringUtils.lowerFirst(slaver.getSimpleName());
                        if (!role.isEmpty()) {
                            name = role;
                        }
                        if (manyToManyPointMap.containsKey(name)) {
                            manyToManyPointMap.get(name).addRole(role);
                        } else {
                            ManyToManyPoint manyToManyPoint = new ManyToManyPoint(clazz, slaver);
                            if (!role.isEmpty()) {
                                manyToManyPoint.addRole(role);
                            }
                            manyToManyPointMap.put(name, manyToManyPoint);
                        }
                        manyToManyFieldList.add(field);
                        fieldTypeMap.put(name, slaver);
//                        MetaTableFactory.addManyToMany(slaver, clazz, role);
                    }
                } else if (field.isAnnotationPresent(OneToMany.class)) {
                    Type fieldType = field.getGenericType();
                    if (fieldType instanceof ParameterizedType) {
                        Class<?> slaver = (Class<?>) ((ParameterizedType) fieldType).getActualTypeArguments()[0];
                        if (!type.equals(slaver)) {
                            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
                            String joinField = oneToMany.joinField();
                            if (joinField.isEmpty()) {
                                joinField = StringUtils.lowerFirst(type.getSimpleName());
                            }
                            String name = StringUtils.lowerFirst(slaver.getSimpleName());
                            if (oneToManyPointMap.containsKey(name)) {
                                oneToManyPointMap.get(name).addSlaver();
                            } else {
                                OneToManyPoint oneToManyPoint = new OneToManyPoint(clazz, slaver);
                                oneToManyPoint.setJoinField(joinField);
                                oneToManyPoint.setOrphanRemoval(oneToMany.orphanRemoval());
                                oneToManyPointMap.put(StringUtils.lowerFirst(slaver.getSimpleName()), oneToManyPoint);
                            }
                        }
                        oneToManyFieldList.add(field);
                    }
                }
                continue;
            }


            MetaColumn metaColumn = new MetaColumn();
            metaColumn.setField(field);

            if (field.isAnnotationPresent(I18n.class)) {
                this.internationalized = true;
            }

            if (field.isAnnotationPresent(Column.class)) {
                Column annotation = field.getAnnotation(Column.class);
                metaColumn.setName(fieldName);
                metaColumn.setComment(annotation.alias());
                metaColumn.setLength(annotation.length());
                metaColumn.setNullable(annotation.nullable());
                metaColumn.setPrecision(annotation.precision());
                metaColumn.setScale(annotation.scale());
                metaColumn.setUnique(annotation.unique());
                metaColumn.setInsertable(annotation.insertable());
                metaColumn.setUpdatable(annotation.updatable());
            } else {
                metaColumn.setName(fieldName);
                metaColumn.setNullable(true);
                metaColumn.setInsertable(true);
                metaColumn.setUpdatable(true);
            }

            if (field.isAnnotationPresent(Id.class)) {
                Id id = field.getAnnotation(Id.class);
                metaColumn.setPrimaryKey(true);
                metaColumn.setNullable(false);
                metaColumn.setGenerator(id.generator());
                metaColumn.setInsertable(true);
                metaColumn.setUpdatable(false);
            } else if (fieldName.equals("id")) {
                metaColumn.setPrimaryKey(true);
                if (Number.class.isAssignableFrom(field.getType())) {
                    metaColumn.setGenerator(Generator.SNOWFLAKE);
                } else {
                    metaColumn.setGenerator(Generator.NONE);
                }
                metaColumn.setNullable(false);
                metaColumn.setInsertable(true);
                metaColumn.setUpdatable(false);
            }

            metaColumn.setAlias(fieldName);
            metaColumn.setType(field.getType());
            fieldTypeMap.put(fieldName, field.getType());

            if (field.getType().isAnnotationPresent(Entity.class)) {
                metaColumn.setName(fieldName + "_id");
                metaColumn.setType(Long.class);
            }
            if (metaColumn.isUnique()) {
                MetaIndex metaIndex = new MetaIndex();
                metaIndex.setName("inx_" + name + "_" + metaColumn.getName());
                metaIndex.setUnique(true);
                metaIndex.setColumns(metaColumn.getName());
                this.metaIndexList.add(metaIndex);
            }
            this.addColumn(metaColumn);

        }
    }

    public Class<?> getFieldTypeByFieldName(String filedName) {
        return fieldTypeMap.get(filedName);
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public ManyToManyPoint getManyToManyPoint(String name) {
        return manyToManyPointMap.get(name);
    }

    public OneToManyPoint getOneToManyPoint(String name) {
        return oneToManyPointMap.get(name);
    }

    public Map<String, OneToManyPoint> getOneToManyPointMap() {
        return oneToManyPointMap;
    }

    public RowMapper<T> getRowMapper() {
        return rowMapper;
    }

    public MetaColumn getPrimaryKey() {
        return primaryKey;
    }

    public Map<String, ManyToManyPoint> getManyToManyPointMap() {
        return manyToManyPointMap;
    }

    public void addColumn(MetaColumn metaColumn) {
        if (metaColumn.isPrimaryKey()) {
            metaColumn.setTableName(name);
            primaryKey = metaColumn;
        } else {
            metaColumnList.add(metaColumn);
        }
        nameMap.put(metaColumn.getAlias(), metaColumn.getName());
    }

    public boolean isInternationalized() {
        return internationalized;
    }

    public String getColumnName(String alias) {
        return nameMap.get(alias);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = SqlParser.toDatabaseName(name);
    }

    public Class<T> getType() {
        return type;
    }

    public void setType(Class<T> type) {
        this.type = type;
    }

    public List<Field> getOneToManyFieldList() {
        return oneToManyFieldList;
    }

    public List<Field> getManyToManyFieldList() {
        return manyToManyFieldList;
    }

    public String select() {
        if (select == null) {
            StringBuilder setter = new StringBuilder();
            for (MetaColumn metaColumn : metaColumnList) {
                setter.append(", ");
                setter.append(metaColumn.getName());
                if (!metaColumn.getName().equals(metaColumn.getAlias())) {
                    setter.append(" as ");
                    setter.append(metaColumn.getAlias());
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("select ");
            sb.append(primaryKey.getName());
            if (!primaryKey.getName().equals(primaryKey.getAlias())) {
                sb.append(" as ");
                sb.append(primaryKey.getAlias());
            }

            sb.append(setter);

            sb.append(" from ");
            sb.append(name);
            select = sb.toString();
        }
        return select;
    }

    public String selectByPrimaryKey() {
        if (selectByPrimaryKey == null) {
            selectByPrimaryKey = select() + " where " + primaryKey.getName() + " = :id";
        }
        return selectByPrimaryKey;
    }

    public String insert(String database) {
        if (insert == null) {
            StringBuilder fields = new StringBuilder();
            StringBuilder values = new StringBuilder();
            if (!primaryKey.getGenerator().equals(Generator.AUTO)) {
                fields.append(", ");
                fields.append(primaryKey.getName());
                values.append(", :");
                values.append(primaryKey.getAlias());
            }
            for (MetaColumn metaColumn : metaColumnList) {
                if ( metaColumn.isInsertable()) {
                    fields.append(", ");
                    fields.append(metaColumn.getName());

                    values.append(", :");
                    values.append(metaColumn.getAlias());
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("insert into ");
            sb.append(name);
            if (fields.length() > 0) {
                sb.append("(");
                sb.append(fields.substring(2));
                sb.append(") values (");
                sb.append(values.substring(2));
                sb.append(")");
            } else {
                sb.append(" default values");
            }

            if (primaryKey != null && primaryKey.getGenerator().equals(Generator.AUTO)) {
                if (database.equals(Database.POSTGRESQL)) {
                    sb.append(" returning ");
                    sb.append(primaryKey.getName());
                }
            }
            insert = sb.toString();
        }
        return insert;
    }


    public String update() {
        if (update == null) {
            StringBuilder setter = new StringBuilder();
            for (MetaColumn metaColumn : metaColumnList) {
                if (metaColumn.isUpdatable()) {
                    setter.append(", ");
                    setter.append(metaColumn.getName());
                    setter.append(" = :");
                    setter.append(metaColumn.getAlias());
                }
            }

            update = "update " + name + " set " + setter.substring(2);
        }
        return update;
    }

    public String updateByPrimaryKey() {
        if (updateByPrimaryKey == null) {
            updateByPrimaryKey = update() + " where " + primaryKey.getName() + " = :" + primaryKey.getAlias();
        }
        return updateByPrimaryKey;
    }

    public String delete() {
        if (delete == null) {
            delete = "delete from " + name;
        }
        return delete;
    }

    public String deleteByPrimaryKey() {
        if (deleteByPrimaryKey == null) {
            deleteByPrimaryKey = delete() + " where " + primaryKey.getName() + " = ?";
        }
        return deleteByPrimaryKey;
    }

    public List<String> generateCreateSql() {
        List<String> sqlList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("\ncreate table ").append(this.name).append(" (\n\t");
        if (primaryKey != null) {
            sb.append(primaryKey.generateText());
        }

        for (MetaColumn metaColumn : metaColumnList) {
            sb.append(",\n\t");
            sb.append(metaColumn.generateText());
        }
        sb.append("\n)");
        sqlList.add(sb.toString());

        for (MetaColumn metaColumn : metaColumnList) {
            if (metaColumn.isUnique()) {
                sqlList.add("create unique index uidx_" + name + "_" + metaColumn.getName() + " on " + name + " (" + metaColumn.getName() + ")");
            }
        }

        return sqlList;
    }


    public List<MetaColumn> getMetaColumnList() {
        return this.metaColumnList;
    }
}
