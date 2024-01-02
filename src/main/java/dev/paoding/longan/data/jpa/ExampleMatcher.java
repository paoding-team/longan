package dev.paoding.longan.data.jpa;

import com.google.common.base.Joiner;
import dev.paoding.longan.data.Between;
import dev.paoding.longan.data.Entity;
import dev.paoding.longan.util.EntityUtils;
import org.springframework.cglib.beans.BeanMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExampleMatcher {
    private final List<Matcher> matcherList = new ArrayList<>();

    public void add(Matcher matcher) {
        matcherList.add(matcher);
    }

    public MatchResult match(String tablePrefix, String columnPrefix, Object object) {
        MetaTable<?> metaTable = MetaTableFactory.get(object.getClass());
        List<String> conditionList = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();
        BeanMap beanMap = BeanMap.create(object);
        for (Matcher matcher : matcherList) {
            String comparer = matcher.getComparer();
            String field = matcher.getField();

            if (comparer.equals("Between")) {
                String columnName = SqlParser.toColumnName(field);
                String start = columnPrefix + "start_" + columnName;
                String end = columnPrefix + "end_" + columnName;
                Between<?> between = matcher.getBetween();
                conditionList.add(tablePrefix + columnName + " between :" + start + " and :" + end);
                paramMap.put(start, between.getStart());
                paramMap.put(end, between.getEnd());
                continue;
            }

            String columnName = metaTable.getColumnName(field);
            String paramName = columnPrefix + columnName;
            switch (comparer) {
                case "IsNull": {
                    conditionList.add(tablePrefix + columnName + " is null");
                    break;
                }
                case "IsNotNull": {
                    conditionList.add(tablePrefix + columnName + " is not null");
                    break;
                }
                case "IsTrue": {
                    conditionList.add(tablePrefix + columnName + " is true");
                    break;
                }
                case "IsFalse": {
                    conditionList.add(tablePrefix + columnName + " is false");
                    break;
                }
            }

            Object value = beanMap.get(field);
            if (value == null) {
                continue;
            }
            Class<?> type = value.getClass();
            if (!type.isPrimitive() && type.isAnnotationPresent(Entity.class)) {
                value = EntityUtils.getId(value);
            }
            switch (comparer) {
                case "Is": {
                    conditionList.add(tablePrefix + columnName + " = :" + paramName);
                    paramMap.put(paramName, value);
                    break;
                }
                case "Containing": {
                    if (type.isArray()) {
                        conditionList.add(tablePrefix + columnName + " && :" + paramName);
                        paramMap.put(paramName, Database.createArrayOf(value));
                    } else {
                        conditionList.add(tablePrefix + columnName + " like :" + paramName);
                        paramMap.put(paramName, "%" + value + "%");
                    }
                    break;
                }
                case "LessThan":
                case "Before": {
                    conditionList.add(tablePrefix + columnName + " < :" + paramName);
                    paramMap.put(paramName, value);
                    break;
                }
                case "LessThanEqual": {
                    conditionList.add(tablePrefix + columnName + " <= :" + paramName);
                    paramMap.put(paramName, value);
                    break;
                }
                case "GreaterThan":
                case "After": {
                    conditionList.add(tablePrefix + columnName + " > :" + paramName);
                    paramMap.put(paramName, value);
                    break;
                }
                case "GreaterThanEqual": {
                    conditionList.add(tablePrefix + columnName + " >= :" + paramName);
                    paramMap.put(paramName, value);
                    break;
                }
                case "StartingWith": {
                    conditionList.add(tablePrefix + columnName + " like :" + paramName);
                    paramMap.put(paramName, value + "%");
                    break;
                }
                case "EndingWith": {
                    conditionList.add(tablePrefix + columnName + " like :" + paramName);
                    paramMap.put(paramName, "%" + value);
                    break;
                }
                case "Not": {
                    conditionList.add(tablePrefix + columnName + " <> :" + paramName);
                    paramMap.put(paramName, value);
                    break;
                }
                case "In": {
                    conditionList.add(tablePrefix + columnName + " in :" + paramName);
                    paramMap.put(paramName, value);
                    break;
                }
                case "NotIn": {
                    conditionList.add(tablePrefix + columnName + " not in :" + paramName);
                    paramMap.put(paramName, value);
                    break;
                }
            }
        }

        MatchResult matchResult = new MatchResult();
        if (conditionList.size() == 0) {
            matchResult.setWhere("");
        } else {
            matchResult.setWhere(Joiner.on(" and ").join(conditionList));
        }
        matchResult.setParamMap(paramMap);

        return matchResult;
    }
}
