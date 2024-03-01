package dev.paoding.longan.data.jpa;


import dev.paoding.longan.data.Between;
import dev.paoding.longan.data.Entity;
import dev.paoding.longan.util.EntityUtils;
import dev.paoding.longan.util.StringUtils;

import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlParser {

    public static Map<String, Object> toMap(Method method, Object[] args) {
        Parameter[] parameters = method.getParameters();
        Map<String, Object> paramMap = new HashMap<>();
        try {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg != null) {
                    Class<?> type = arg.getClass();
                    if (type.isAnnotationPresent(Entity.class)) {
                        Field[] declaredFields = arg.getClass().getDeclaredFields();
                        for (Field field : declaredFields) {
                            if (field.getModifiers() == Modifier.PRIVATE) {
                                if (field.getType().isAnnotationPresent(Entity.class)) {
                                    break;
                                }
                                if (Collection.class.isAssignableFrom(field.getType())) {
                                    Type fieldType = field.getGenericType();
                                    if (fieldType instanceof ParameterizedType) {
                                        Class<?> subType = (Class<?>) ((ParameterizedType) fieldType).getActualTypeArguments()[0];
                                        if (subType.isAnnotationPresent(Entity.class)) {
                                            break;
                                        }
                                    }
                                }
                                field.setAccessible(true);
                                Object value = field.get(arg);
                                if (value != null) {
//                                    paramMap.put(SqlParser.toDatabaseName(parameters[i].getName() + "_" + field.getName()), value);
                                    paramMap.put(parameters[i].getName() + "." + field.getName(), value);
                                }
                            }
                        }
//                    } else if (Between.class.isAssignableFrom(arg.getClass())) {
//                        Between<?> between = (Between<?>) arg;
//                        paramMap.put(SqlParser.toDatabaseName(parameters[i].getName() + "_start"), between.getStart());
//                        paramMap.put(SqlParser.toDatabaseName(parameters[i].getName() + "_end"), between.getEnd());
//                    } else if (type.isArray()) {
//                        paramMap.put(SqlParser.toDatabaseName(parameters[i].getName()), Database.createArrayOf(arg));
                    } else {
//                        paramMap.put(SqlParser.toDatabaseName(parameters[i].getName()), arg);
                        paramMap.put(parameters[i].getName(), arg);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return paramMap;
    }

    public static String getLinkTable(String source, String target, String role) {
        source = toDatabaseName(source);
        target = toDatabaseName(target);
        if (role == null) {
            role = "";
        } else if (!role.isEmpty()) {
            role = "_" + role;
        }
        if (source.compareTo(target) < 0) {
            return source + "_" + target + role;
        } else {
            return target + "_" + source + role;
        }
    }

    public static String toCountSql(String source, String target, String role) {
        if (!role.isEmpty()) {
            role = "_" + role;
        }
        if (source.compareTo(target) < 0) {
            String tableName = source + "_" + target + role;
            return "select count(1) from " + tableName + " where " + source + "_id = :" + source + "_id and " + target + "_id = :" + target + "_id";
        } else {
            String tableName = target + "_" + source + role;
            return "select count(1) from " + tableName + " where " + target + "_id = :" + target + "_id and " + source + "_id = :" + source + "_id";
        }
    }

    public static String toJoinSql(String source, String target, String role) {
        if (!role.isEmpty()) {
            role = "_" + role;
        }
        if (source.compareTo(target) < 0) {
            String tableName = source + "_" + target + role;
            return "insert into " + tableName + " (" + source + "_id, " + target + "_id) values (:" + source + "_id, :" + target + "_id)";
        } else {
            String tableName = target + "_" + source + role;
            return "insert into " + tableName + " (" + target + "_id, " + source + "_id) values (:" + target + "_id, :" + source + "_id)";
        }
    }

    public static String toSplitSql(String source, String target, String role) {
        if (!role.isEmpty()) {
            role = "_" + role;
        }
        if (source.compareTo(target) < 0) {
            String tableName = source + "_" + target + role;
            return "delete from " + tableName + " where " + source + "_id = :" + source + "_id and " + target + "_id = :" + target + "_id";
        } else {
            String tableName = target + "_" + source + role;
            return "delete from " + tableName + " where " + target + "_id = :" + target + "_id and " + source + "_id = :" + source + "_id";
        }
    }

    public static String toSplitSqlAll(String source, String target, String role) {
        if (!role.isEmpty()) {
            role = "_" + role;
        }
        String tableName;
        if (source.compareTo(target) < 0) {
            tableName = source + "_" + target + role;
        } else {
            tableName = target + "_" + source + role;
        }
        return "delete from " + tableName + " where " + source + "_id = :" + source + "_id";
    }

    public static String toColumnName(String name) {
        return StringUtils.lower(name);
    }

    public static String toDatabaseName(String name) {
        int j = name.indexOf("$");
        if (j > 0) {
            name = name.substring(0, j);
        }

        return toColumnName(name);
//        StringBuilder sb = new StringBuilder();
//        sb.append(name.charAt(0));
//        for (int i = 1; i < name.length(); i++) {
//            char chr = name.charAt(i);
//            if (Character.isUpperCase(chr)) {
//                sb.append("_");
//            }
//            sb.append(chr);
//        }
//        return sb.toString().toLowerCase();
    }

//    public static String parse(String method, MetaTable<?> table) {
//        String order = new String();
//        String where;
//        if (method.contains("OrderBy")) {
//            int i = method.indexOf("OrderBy");
//            order = parseOrder(method.substring(i), table);
//            where = method.substring(0, i);
//        } else {
//            where = method;
//        }
//
//        return parseWhere(where, table) + order;
//
//    }

//    public static String parseOrder(String order, MetaTable<?> table) {
//        StringBuilder orderBuilder = new StringBuilder();
//        orderBuilder.append(" order by ");
//        if (order.endsWith("Desc")) {
//            orderBuilder.append(StringUtils.toColumnName(order.substring(7, order.length() - 4)));
//            orderBuilder.append(" desc");
//        } else {
//            orderBuilder.append(StringUtils.toColumnName(order.substring(7)));
//        }
//
//        return orderBuilder.toString();
//    }

//    public static String parseWhere(String where, MetaTable<?> table) {
//        int i;
//        List<String> connectors = new ArrayList<>();
//        StringBuilder whereBuilder = new StringBuilder();
//        while ((i = where.indexOf("And")) != -1 && Character.isUpperCase(where.charAt(i + 3))) {
//            whereBuilder.append(where, 0, i);
//            whereBuilder.append("|");
//            where = where.substring(i + 3);
//            connectors.add("and");
//        }
//        whereBuilder.append(where);
//        where = whereBuilder.toString();
//        whereBuilder = new StringBuilder();
//        while ((i = where.indexOf("Or")) != -1 && Character.isUpperCase(where.charAt(i + 2))) {
//            whereBuilder.append(where, 0, i);
//            whereBuilder.append("|");
//            where = where.substring(i + 2);
//            connectors.add("or");
//        }
//        whereBuilder.append(where);
//        where = whereBuilder.toString();
//        String[] array = where.split("\\|");
//        whereBuilder = new StringBuilder();
//        for (int j = 0; j < array.length; j++) {
//            String statement = array[j];
//            whereBuilder.append(" ");
//            whereBuilder.append(parseOperator(statement, table));
//            if (j < connectors.size()) {
//                whereBuilder.append(" ");
//                whereBuilder.append(connectors.get(j));
//            }
//        }
//        return whereBuilder.toString();
//    }

//    private static String parseOperator(String statement, MetaTable<?> table) {
//        List<String> list = ImmutableList.of("Between", "LessThan", "LessThanEqual", "GreaterThan", "GreaterThanEqual",
//                "After", "Before", "IsNull", "IsNotNull", "NotNull", "Like", "NotLike", "Not", "True", "False");
//
//        for (String operator : list) {
//            if (statement.endsWith(operator)) {
//                statement = statement.substring(0, statement.length() - operator.length());
//                return parseOperator(statement, operator, table);
//            }
//        }
//
//        return parseOperator(statement, "Is", table);
//    }

//    private static String parseOperator(String key, String operator, MetaTable<?> table) {
//        String field = StringUtils.toColumnName(key);
//        String column = table.getColumnName(field);
//        if (operator.equals("Is")) {
//            return column + " = ?";
//        } else if (operator.equals("Between")) {
//            return column + " between ? and ?";
//        } else if (operator.equals("LessThan")) {
//            return column + " < ?";
//        } else if (operator.equals("LessThanEqual")) {
//            return column + " <= ?";
//        } else if (operator.equals("GreaterThan")) {
//            return column + " > ?";
//        } else if (operator.equals("GreaterThanEqual")) {
//            return column + " >= ?";
//        } else if (operator.equals("After")) {
//            return column + " > ?";
//        } else if (operator.equals("Before")) {
//            return column + " < ?";
//        } else if (operator.equals("IsNull")) {
//            return column + " is null";
//        } else if (operator.equals("IsNotNull") || operator.equals("NotNull")) {
//            return column + " is not null";
//        } else if (operator.equals("Like")) {
//            return column + " like ?";
//        } else if (operator.equals("NotLike")) {
//            return column + " not like ?";
//        } else if (operator.equals("Not")) {
//            return column + " <> ?";
//        } else if (operator.equals("True")) {
//            return column + " is true";
//        } else if (operator.equals("False")) {
//            return column + " is false ";
//        }
//        throw new RuntimeException("not support keyword " + operator);
//    }

    /**
     * 根据传入的参数来解析动态 SQL
     *
     * @param sql    动态 SQL
     * @param params 参数
     * @return 解析后的 SQL
     */
    public static String parseDynamicSql(String sql, Map<String, Object> params) {
//        String regex = "[A-Za-z0-9_.]+\\s+(=|>|<|>=|<=|<>|like|is|in|not|not\\s+like|not\\s+in|between\\s+:(\\S+)\\?\\s+and)\\s+\\(?:(\\S+)\\?\\)?";
        String regex = "[A-Za-z0-9_.]+\\s+(=|>|<|>=|<=|<>|like|is|in|not|not\\s+like|not\\s+in|between\\s+:(\\S+)\\s+and)\\s+\\(?:(\\S+)\\)?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            String text = matcher.group(0);
            String firstParameter = matcher.group(2);
            String secondParameter = matcher.group(3);
            if (firstParameter != null && secondParameter != null) {
                if (params.get(firstParameter) == null || params.get(secondParameter) == null) {
                    sql = sql.replace(text, "#");
                }
            } else if (secondParameter != null) {
                if (matcher.group(1).endsWith("like")) {
                    String key = secondParameter.replaceAll("%", "");
                    if (params.get(key) == null) {
                        sql = sql.replace(text, "#");
                    }
                } else {
                    if (params.get(secondParameter) == null) {
                        sql = sql.replace(text, "#");
                    }
                }
            }
        }
        regex = "(and|or)\\s+#";
        sql = sql.replaceAll(regex, "");
        regex = "#\\s+(and|or)";
        sql = sql.replaceAll(regex, "");
        regex = "(and|or)\\s+\\(+\\s*#\\s*\\)+";
        sql = sql.replaceAll(regex, "");
        regex = "\\s+\\(+\\s*#\\s*\\)+\\s+(and|or)";
        sql = sql.replaceAll(regex, "");
        regex = "\\s+where\\s+\\(?\\s*#\\s*\\)?";
        sql = sql.replaceAll(regex, "");
        regex = "\\?";
        sql = sql.replaceAll(regex, "");
        sql = parseStaticSql(sql, params);
        return sql;
    }

    public static String parseStaticSql(String sql, Map<String, Object> params) {
        String regex = "[A-Za-z0-9_.]+\\s+(like|in|not\\s+like|not\\s+in)\\s+\\(?:([A-Za-z0-9_%]+)\\)?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            String secondParameter = matcher.group(2);
            if (secondParameter != null) {
                if (matcher.group(1).endsWith("like")) {
                    String key = secondParameter.replaceAll("%", "");
                    sql = sql.replace(secondParameter, key);
                    if (params.get(key) != null) {
                        String value = params.get(key).toString();
                        if (secondParameter.startsWith("%") && !value.startsWith("%")) {
                            value = "%" + value;
                        }
                        if (secondParameter.endsWith("%") && !value.endsWith("%")) {
                            value += "%";
                        }
                        params.put(key, value);
                    }
                } else if (matcher.group(1).endsWith("in")) {
                    Collection<?> collection = (Collection<?>) params.get(secondParameter);
                    if (collection.size() == 0) {
                        params.put(secondParameter, null);
                    } else {
                        if (collection.iterator().next().getClass().isAnnotationPresent(Entity.class)) {
                            List<Object> idList = new ArrayList<>();
                            for (Object object : collection) {
                                idList.add(EntityUtils.getId(object));
                            }
                            params.put(secondParameter, idList);
                        }
                    }
                }

            }
        }
        return sql;
    }

}
