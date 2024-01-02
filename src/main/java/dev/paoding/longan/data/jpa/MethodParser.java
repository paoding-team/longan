package dev.paoding.longan.data.jpa;

import com.google.common.collect.ImmutableList;
import dev.paoding.longan.data.Between;
import dev.paoding.longan.data.Entity;
import dev.paoding.longan.data.Pageable;
import dev.paoding.longan.util.EntityUtils;
import dev.paoding.longan.util.StringUtils;
import org.springframework.cglib.beans.BeanMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodParser {
    private static final List<String> operatorList = ImmutableList.of("Between", "LessThan", "LessThanEqual", "GreaterThan", "GreaterThanEqual",
            "After", "Before", "IsNull", "IsNotNull", "NotNull", "Null", "StartingWith", "EndingWith", "Containing", "Not", "NotIn", "In", "IsTrue", "IsFalse", "True", "False");
    private String sql = "";
    private MetaTable<?> metaTable;
    private Class<?> entity;
    private String model;
    private Map<String, Object> paramMap = new HashMap<>();

    public static MethodParser of(String action, Class<?> type, String statement, Map<String, Object> initialParamMap) {
        return of(action, type, statement, initialParamMap, false);
    }

    public static MethodParser of(String action, Class<?> type, String statement, Map<String, Object> initialParamMap, boolean distinct) {
        MethodParser methodParser = new MethodParser();
        methodParser.metaTable = MetaTableFactory.get(type);
        methodParser.entity = type;
        methodParser.model = StringUtils.lowerFirst(type.getSimpleName());
        if (action.equals("select")) {
            methodParser.parse(statement, initialParamMap);
            if (distinct) {
                methodParser.sql = "select distinct t0.* " + methodParser.sql;
            } else {
                methodParser.sql = "select t0.* " + methodParser.sql;
            }
            if (initialParamMap.containsKey("pageable")) {
                Pageable pageable = (Pageable) initialParamMap.get("pageable");
                methodParser.sql += pageable.toSql();
            }
        } else if (action.equals("count")) {
            methodParser.parse(statement, initialParamMap);
            methodParser.sql = "select count(1) " + methodParser.sql;
        } else if (action.equals("delete")) {
            methodParser.delete(statement, initialParamMap);
            methodParser.sql = "delete " + methodParser.sql;
        }
        return methodParser;
    }

    private void delete(String statement, Map<String, Object> initialParamMap) {
        List<SearchField> searchFieldList = getSearchFieldList(statement, initialParamMap);

        boolean isJoin = false;
        boolean enableConnector = false;
        sql = "from " + metaTable.getName();
        String condition = "";
        for (SearchField searchField : searchFieldList) {
            if (!searchField.isManyToMany()) {
                String fieldName = searchField.getFieldName();
                String columnName = searchField.getColumnName();
                Object value = initialParamMap.get(searchField.getFieldName());
                if (value == null) {
                    condition += " " + searchField.toSql("", "", enableConnector);
                } else {
                    if (value.getClass().isAnnotationPresent(Entity.class)) {
                        condition +=  searchField.getConnector(enableConnector) + " " + columnName + "_id = :" + columnName + "_id";
                        paramMap.put(columnName + "_id", BeanMap.create(initialParamMap.get(fieldName)).get("id"));
//                    } else if (Example.class.isAssignableFrom(value.getClass())) {
//                        Example<?> example = (Example<?>) value;
//                        MatchResult matchResult = example.match("");
//                        condition += " " + searchField.getConnector() + " " + matchResult.getWhere();
//                        paramMap.putAll(matchResult.getParamMap());
//                        continue;
                    } else {
                        paramMap.put(columnName, value);
                        condition += " " + searchField.toSql("", "", enableConnector);
                    }
                }

                enableConnector = true;
            }
        }
        if (isJoin) {
            sql += condition;
        } else {
            if (!condition.isEmpty()) {
                sql += " where" + condition;
            }
        }
    }

    private void parse(String statement, Map<String, Object> initialParamMap) {
        StringBuilder orderBuilder = null;
        if (statement.contains("OrderBy")) {
            orderBuilder = new StringBuilder();
            int i = statement.indexOf("OrderBy");
            String order = statement.substring(i);
            orderBuilder.append(" order by ");
            if (order.endsWith("Desc")) {
                String field = StringUtils.lowerFirst(order.substring(7, order.length() - 4));
                orderBuilder.append(metaTable.getColumnName(field));
                orderBuilder.append(" desc");
            } else if (order.endsWith("Asc")) {
                String field = StringUtils.lowerFirst(order.substring(7, order.length() - 3));
                orderBuilder.append(metaTable.getColumnName(field));
                orderBuilder.append(" asc");
            } else {
                String field = StringUtils.lowerFirst(order.substring(7));
                orderBuilder.append(metaTable.getColumnName(field));
            }

            statement = statement.substring(0, i);
        }

        List<SearchField> searchFieldList = getSearchFieldList(statement, initialParamMap);

        boolean enableConnector = false;
        sql = "from " + metaTable.getName() + " t0";
        String condition = "";
        int i = 0;
        for (SearchField searchField : searchFieldList) {
            String fieldName = searchField.getFieldName();
            String fileDbName = searchField.getFileDbName();
            String columnName = searchField.getColumnName();
            if (searchField.isManyToMany()) {
                i++;
                if (searchField.getType().isAnnotationPresent(Entity.class)) {
                    sql += " left join " + SqlParser.getLinkTable(metaTable.getAlias(), columnName, searchField.getRole()) + " t" + i;
                    sql += " on t0.id = t" + i + "." + metaTable.getAlias() + "_id";
                    if (searchField.getOperator().equals("In")) {
                        condition += searchField.getConnector(enableConnector) + " t" + i + "." + columnName + "_id in (:" + columnName + "_id_list)";
                        paramMap.put(columnName + "_id_list", initialParamMap.get(fieldName + "List"));
                    } else if (searchField.getOperator().equals("NotIn")) {
                        condition += searchField.getConnector(enableConnector) + " t" + i + "." + columnName + "_id not in (:" + columnName + "_id_list)";
                        paramMap.put(columnName + "_id_list", initialParamMap.get(fieldName + "List"));
                    } else {
                        condition += searchField.getConnector(enableConnector) + " t" + i + "." + columnName + "_id = :" + columnName + "_id";
                        paramMap.put(columnName + "_id", BeanMap.create(initialParamMap.get(fieldName)).get("id"));
                    }
                    enableConnector = true;
                } else if (Example.class.isAssignableFrom(searchField.getType())) {
                    Example<?> example = (Example<?>) initialParamMap.get(fieldName);
                    if (!example.isEmpty()) {
                        sql += " left join " + SqlParser.getLinkTable(metaTable.getAlias(), columnName, searchField.getRole()) + " t" + i;
                        sql += " on t0.id = t" + i + "." + metaTable.getAlias() + "_id";
                        i++;
                        MatchResult matchResult = example.match("t" + i, fieldName);
                        sql += " left join " + MetaTableFactory.get(example.getEntity().getClass()).getName() + " t" + i;
                        sql += " on t" + i + ".id = t" + (i - 1) + "." + columnName + "_id";
                        condition += searchField.getConnector(enableConnector) + " " + matchResult.getWhere();

                        paramMap.putAll(matchResult.getParamMap());
                        enableConnector = true;
                    }
                } else {
                    throw new RuntimeException("Type of " + fieldName + " is " + searchField.getType().getName() + ", it must be Entity or Example.");
                }
            } else if (searchField.isOneToMany()) {
                i++;
                if (searchField.getType().isAnnotationPresent(Entity.class)) {
                    sql += " left join " + MetaTableFactory.get(searchField.getType()).getName() + " t" + i;
                    sql += " on t0.id = t" + i + "." + columnName + "_id";
                    if (searchField.getOperator().equals("In")) {
                        condition += searchField.getConnector(enableConnector) + " t" + i + ".id in (:" + fileDbName + "_id_list)";
                        paramMap.put(fieldName + "_id_list", initialParamMap.get(fieldName + "List"));
                    } else if (searchField.getOperator().equals("NotIn")) {
                        condition += searchField.getConnector(enableConnector) + " t" + i + ".id not in (:" + fileDbName + "_id_list)";
                        paramMap.put(fieldName + "_id_list", initialParamMap.get(fieldName + "List"));
                    } else {
                        condition += searchField.getConnector(enableConnector) + " t" + i + ".id = :" + fileDbName + "_id";
                        paramMap.put(fieldName + "_id", BeanMap.create(initialParamMap.get(fieldName)).get("id"));
                    }
                    enableConnector = true;
                } else if (Example.class.isAssignableFrom(searchField.getType())) {
                    Example<?> example = (Example<?>) initialParamMap.get(fieldName);
                    if (!example.isEmpty()) {
                        String joinField = example.getJoinField();
                        if (joinField == null) {
                            joinField = columnName;
                        } else {
                            joinField = SqlParser.toDatabaseName(joinField);
                        }
                        MatchResult matchResult = example.match("t" + i, SqlParser.toDatabaseName(fieldName));
                        String where = matchResult.getWhere();
                        sql += " left join " + MetaTableFactory.get(example.getEntity().getClass()).getName() + " t" + i;
                        sql += " on t0.id = t1." + joinField + "_id";
                        if (where.isEmpty()) {
                            enableConnector = false;
                        } else {
                            condition += searchField.getConnector(enableConnector) + " " + matchResult.getWhere();
                            enableConnector = true;
                        }
                        paramMap.putAll(matchResult.getParamMap());
                    }
                } else {
                    throw new RuntimeException("Type of " + fieldName + " is " + searchField.getType().getName() + ", it must be Entity or Example.");
                }
            } else if (searchField.isManyToOne()) {
                i++;
                if (searchField.isNoParam()) {
                    condition += searchField.getConnector(enableConnector) + " t0." + columnName + "_id" + searchField.getCondition();
                    enableConnector = true;
                } else if (searchField.getType().isAnnotationPresent(Entity.class)) {
                    if (searchField.getOperator().equals("In")) {
                        condition += searchField.getConnector(enableConnector) + " t0." + columnName + "_id in (:" + columnName + "_id_list)";
                        paramMap.put(columnName + "_id_list", initialParamMap.get(fieldName + "List"));
                    } else if (searchField.getOperator().equals("NotIn")) {
                        condition += searchField.getConnector(enableConnector) + " t0." + columnName + "_id not in (:" + columnName + "_id_list)";
                        paramMap.put(columnName + "_id_list", initialParamMap.get(fieldName + "List"));
                    } else {
                        condition += searchField.getConnector(enableConnector) + " t0." + columnName + "_id = :" + columnName + "_id";
                        paramMap.put(columnName + "_id", BeanMap.create(initialParamMap.get(fieldName)).get("id"));
                    }
                    enableConnector = true;
                } else if (Example.class.isAssignableFrom(searchField.getType())) {
                    Example<?> example = (Example<?>) initialParamMap.get(fieldName);
                    if (!example.isEmpty()) {
                        MatchResult matchResult = example.match("t" + i, SqlParser.toDatabaseName(fieldName));
                        String where = matchResult.getWhere();
                        sql += " left join " + MetaTableFactory.get(example.getEntity().getClass()).getName() + " t" + i;
                        sql += " on t" + i + ".id = t0." + columnName + "_id";
                        if (where.isEmpty()) {
                            enableConnector = false;
                        } else {
                            condition += searchField.getConnector(enableConnector) + " " + matchResult.getWhere();
                            enableConnector = true;
                        }
                        paramMap.putAll(matchResult.getParamMap());
                    }
                } else {
                    throw new RuntimeException("Type of " + fieldName + " is " + searchField.getType().getName() + ", it must be Entity or Example.");
                }
            } else {
                Object value = initialParamMap.get(fieldName);
                if (searchField.getOperator().equals("In") || searchField.getOperator().equals("NotIn")) {
                    condition += " " + searchField.toSql("t0.", model + "_", enableConnector);
                    paramMap.put(model + "_" + fileDbName + "_list", initialParamMap.get(fieldName + "List"));
                    enableConnector = true;
                } else if (value == null) {
                    condition += " " + searchField.toSql("t0.", model + "_", enableConnector);
                    enableConnector = true;
                } else {
                    if (searchField.getType().isAnnotationPresent(Entity.class)) {
                        condition += searchField.getConnector(enableConnector) + " t0.id = :" + SqlParser.toDatabaseName(fieldName) + "_id";
                        paramMap.put(columnName + "_id", BeanMap.create(initialParamMap.get(fieldName)).get("id"));
                        enableConnector = true;
                    } else if (Example.class.isAssignableFrom(searchField.getType())) {
                        Example<?> example = (Example<?>) initialParamMap.get(fieldName);
                        if (!example.isEmpty()) {
                            MatchResult matchResult = example.match("t0");
                            String where = matchResult.getWhere();
                            if (!where.isEmpty()) {
                                condition += searchField.getConnector(enableConnector) + " " + matchResult.getWhere();
                                paramMap.putAll(matchResult.getParamMap());
                                enableConnector = true;
                            }
                        }
                    } else if (searchField.getOperator().equals("Between")) {
                        condition += " " + searchField.toSql("t0.", model + "_", enableConnector);
                        Between<?> between = (Between) value;
                        paramMap.put(model + "_" + fileDbName + "_start", between.getStart());
                        paramMap.put(model + "_" + fileDbName + "_end", between.getEnd());
                        enableConnector = true;
                    }else if(Enum.class.isAssignableFrom(searchField.getType())){
                        condition += " " + searchField.toSql("t0.", model + "_", enableConnector);
                        paramMap.put(model + "_" + fileDbName, value.toString());
                        enableConnector = true;
                    } else {
                        condition += " " + searchField.toSql("t0.", model + "_", enableConnector);
                        paramMap.put(model + "_" + fileDbName, value);
                        enableConnector = true;
                    }
                }

            }
        }
        if (!condition.isEmpty()) {
            sql += " where" + condition;
        }
        if (orderBuilder != null) {
            sql += orderBuilder.toString();
        }
    }

    private List<SearchField> getSearchFieldList(String statement, Map<String, Object> initialParamMap) {
        char[] chars = statement.toCharArray();
        StringBuilder sb = new StringBuilder();
        List<String> connectorList = new ArrayList<>();
        List<String> segmentList = new ArrayList<>();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == 'A' && chars[i + 1] == 'n' && chars[i + 2] == 'd' && Character.isUpperCase(chars[i + 3])) {
                connectorList.add("and");
                segmentList.add(sb.toString());
                sb = new StringBuilder();
                i = i + 2;
            } else if (chars[i] == 'O' && chars[i + 1] == 'r' && Character.isUpperCase(chars[i + 2])) {
                connectorList.add("or");
                segmentList.add(sb.toString());
                sb = new StringBuilder();
                i = i + 1;
            } else {
                sb.append(chars[i]);
            }
        }
        segmentList.add(sb.toString());


        List<SearchField> searchFieldList = new ArrayList<>();
        for (int i = 0; i < segmentList.size(); i++) {
            String segment = StringUtils.lowerFirst(segmentList.get(i));
            boolean matched = false;

            SearchField searchField = new SearchField();
            for (String operator : operatorList) {
                if (segment.endsWith(operator)) {
                    String fieldName = segment.substring(0, segment.length() - operator.length());
                    searchField.setFieldName(fieldName);
                    searchField.setOperator(operator);

                    if (operator.equals("IsNull") || operator.equals("Null") || operator.equals("IsNotNull") || operator.equals("NotNull") ||
                            operator.equals("IsTrue") || operator.equals("True") || operator.equals("IsFalse") || operator.equals("False")) {
                        searchField.setNoParam(true);
                    } else if (operator.equals("StartingWith")) {
                        String value = (String) initialParamMap.get(fieldName);
                        if (value != null) {
                            value = value.trim();
                            if (value.isEmpty()) {
                                initialParamMap.remove(fieldName);
                            } else {
                                initialParamMap.put(fieldName, value + "%");
                            }
                        }
                    } else if (operator.equals("EndingWith")) {
                        String value = (String) initialParamMap.get(fieldName);
                        if (value != null) {
                            value = value.trim();
                            if (value.isEmpty()) {
                                initialParamMap.remove(fieldName);
                            } else {
                                initialParamMap.put(fieldName, "%" + value);
                            }
                        }
                    } else if (operator.equals("Containing")) {
                        String value = (String) initialParamMap.get(fieldName);
                        if (value != null) {
                            value = value.trim();
                            if (value.isEmpty()) {
                                initialParamMap.remove(fieldName);
                            } else {
                                initialParamMap.put(fieldName, "%" + value + "%");
                            }
                        }
                    } else if (operator.endsWith("In")) {
                        String fieldListName = fieldName + "List";
                        if (initialParamMap.get(fieldListName) != null) {
                            List<?> list = (List<?>) initialParamMap.get(fieldListName);
                            if (list.size() > 0) {
                                Class<?> type = list.get(0).getClass();
                                if (BeanProxy.class.isAssignableFrom(type) || type.isAnnotationPresent(Entity.class)) {
                                    List<Object> idList = new ArrayList<>();
                                    for (Object object : list) {
                                        idList.add(EntityUtils.getId(object));
                                    }
                                    initialParamMap.put(fieldListName, idList);
                                } else {
                                    initialParamMap.put(fieldListName, list);
                                }
                            }
                            if (list.size() == 0) {
                                initialParamMap.put(fieldListName, null);
                            }
                            searchFieldList.add(searchField);
                        }
                    }
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                searchField.setFieldName(segment);
                searchField.setOperator("Is");
            }

            if (i > 0) {
                searchField.setConnector(connectorList.get(i - 1));
            }

            String fieldName = searchField.getFieldName();
            if (metaTable.getColumnName(fieldName) == null) {
                if (metaTable.getManyToManyPoint(fieldName) != null) {
                    ManyToManyPoint manyToManyPoint = metaTable.getManyToManyPoint(fieldName);
                    if (manyToManyPoint.hasRole()) {
                        searchField.setColumnName(SqlParser.toColumnName(metaTable.getFieldTypeByFieldName(fieldName).getSimpleName()));
                        searchField.setRole(fieldName);
                    }
                    searchField.setType(manyToManyPoint.getSlaver());
                    searchField.setManyToMany(true);
                } else if (metaTable.getOneToManyPoint(fieldName) != null) {
                    OneToManyPoint oneToManyPoint = metaTable.getOneToManyPoint(fieldName);
                    searchField.setType(oneToManyPoint.getSlaver());
                    if (oneToManyPoint.getSlaverQuantity() == 1) {
                        String joinField = oneToManyPoint.getJoinField();
                        searchField.setColumnName(SqlParser.toDatabaseName(joinField));
                    } else {
                        searchField.setColumnName(null);
                    }
                    searchField.setOneToMany(true);
                } else {
                    if (!fieldName.equals(StringUtils.lowerFirst(entity.getSimpleName()))) {
                        throw new RuntimeException("not found " + fieldName + " field on " + entity.getSimpleName());
                    }
                }
            } else {
                if (metaTable.getFieldTypeByFieldName(fieldName).isAnnotationPresent(Entity.class)) {
                    searchField.setManyToOne(true);
                    searchField.setType(metaTable.getFieldTypeByFieldName(fieldName));
//                    searchField.setColumnName(SqlParser.toDatabaseName(fieldName) + "_id");
                }
            }


            if (initialParamMap.containsKey(fieldName)) {
                Object value = initialParamMap.get(fieldName);
                if (value != null) {
                    Class<?> type = value.getClass();
                    if (Example.class.isAssignableFrom(type)) {
                        Example<?> example = (Example<?>) value;
                        if (example.isDisabled()) {
                            if (example.getJoinField() != null) {
                                searchField.setColumnName(SqlParser.toDatabaseName(example.getJoinField()));
                            }
                            searchField.setType(example.getEntity().getClass());
                            initialParamMap.put(fieldName, example.getEntity());
                        } else {
                            searchField.setType(type);
                        }
                    } else {
                        if (searchField.getType() == null) {
                            searchField.setType(type);
                        }
                    }
                    searchFieldList.add(searchField);
                }
            } else if (searchField.isNoParam()) {
                searchField.setType(metaTable.getFieldTypeByFieldName(fieldName));
                searchFieldList.add(searchField);
            }
        }

        return searchFieldList;
    }

    public String getSql() {
        return this.sql;
    }

    public Map<String, Object> getParamMap() {
        return this.paramMap;
    }
}
