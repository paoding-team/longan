package dev.paoding.longan.data.jpa;

public class SearchField {

    private String modelName;
    private String fieldName;
    private String filedDbName;
    private String columnName;
    private String operator;
    private boolean manyToMany;
    private boolean oneToMany;
    private boolean manyToOne;
    //    private boolean shadow;
    private boolean noParam;
    private Class<?> type;
    private String connector;
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCondition() {
        if (operator != null) {
            if (operator.equals("IsNull") || operator.equals("Null")) {
                return " is null";
            } else if (operator.equals("IsNotNull") || operator.equals("NotNull")) {
                return " is not null";
            } else if (operator.equals("IsTrue") || operator.equals("True")) {
                return " is true";
            } else if (operator.equals("IsFalse") || operator.equals("False")) {
                return " is false";
            }
        }
        return "";
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getConnector() {
        return connector;
    }

    public String getConnector(boolean enableConnector) {
        if (enableConnector) {
            return " " + connector;
        }
        return "";
    }

    public void setConnector(String connector) {
        this.connector = connector;
    }

    public boolean isNoParam() {
        return noParam;
    }

    public void setNoParam(boolean noParam) {
        this.noParam = noParam;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
        this.columnName = SqlParser.toDatabaseName(fieldName);
        this.filedDbName = SqlParser.toDatabaseName(fieldName);
    }

    public String getFileDbName() {
        return this.filedDbName;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public boolean isManyToMany() {
        return manyToMany;
    }

    public void setManyToMany(boolean many) {
        this.manyToMany = many;
    }

    public boolean isOneToMany() {
        return oneToMany;
    }

    public void setOneToMany(boolean one) {
        this.oneToMany = one;
    }

    public boolean isManyToOne() {
        return manyToOne;
    }

    public void setManyToOne(boolean manyToOne) {
        this.manyToOne = manyToOne;
    }

//    public boolean isShadow() {
//        return shadow;
//    }
//
//    public void setShadow(boolean shadow) {
//        this.shadow = shadow;
//    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String toSql(String tablePrefix, String columnPrefix, boolean enableConnector) {
        String sql = (enableConnector ? connector + " " : "") + tablePrefix + columnName;
        switch (operator) {
            case "Is": {
                return sql + " = :" + columnPrefix + columnName;
            }
            case "IsNull": {
                return sql + " is null";
            }
            case "IsNotNull": {
                return sql + " is not null";
            }
            case "IsTrue": {
                return sql + " is true";
            }
            case "IsFalse": {
                return sql + " is false";
            }
            case "Containing": {
                return sql + " like :" + columnPrefix + columnName;
            }
            case "LessThan": {
                return sql + " < :" + columnPrefix + columnName;
            }
            case "LessThanEqual": {
                return sql + " <= :" + columnPrefix + columnName;
            }
            case "GreaterThan": {
                return sql + " > :" + columnPrefix + columnName;
            }
            case "GreaterThanEqual": {
                return sql + " >= :" + columnPrefix + columnName;
            }
            case "After": {
                return sql + " > :" + columnPrefix + columnName;
            }
            case "Before": {
                return sql + " < :" + columnPrefix + columnName;
            }
            case "StartingWith": {
                return sql + " like :" + columnPrefix + columnName;
            }
            case "EndingWith": {
                return sql + " like :" + columnPrefix + columnName;
            }
            case "Not": {
                return sql + " <> :" + columnPrefix + columnName;
            }
            case "In": {
                return sql + " in (:" + columnPrefix + columnName + "_list)";
            }
            case "NotIn": {
                return sql + " not in (:" + columnPrefix + columnName + "_list)";
            }
            case "Between": {
                return sql + " between :" + columnPrefix + columnName + "_start and :" + columnPrefix + columnName + "_end";
            }
        }
        return "";
    }

    @Override
    public String toString() {
        return "SearchField{" +
                "name='" + fieldName + '\'' +
                ", operator='" + operator + '\'' +
                ", many=" + manyToMany +
                ", noParam=" + noParam +
                ", type=" + type +
                ", connector='" + connector + '\'' +
                '}';
    }
}
