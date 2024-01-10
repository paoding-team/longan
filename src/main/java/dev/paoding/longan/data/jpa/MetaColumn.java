package dev.paoding.longan.data.jpa;

import dev.paoding.longan.service.SystemException;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class MetaColumn {
    private String tableName;
    private String name;
    private String alias;
    private String comment;
    private int length = 64;
    private boolean nullable;
    private boolean insertable;
    private boolean updatable;
    private boolean unique;
    private boolean primaryKey;
    private int scale;
    private int precision;
    private Generator generator;
    private Class<?> type;
    private Field field;

    public Generator getGenerator() {
        return generator;
    }

    public void setGenerator(Generator generator) {
        this.generator = generator;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
        this.field.setAccessible(true);
    }

    public Class<?> getType() {
        return type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Object getValue(Object object) {
        try {
            return this.field.get(object);
        } catch (IllegalAccessException e) {
            throw new SystemException(e);
        }
    }

    public void setType(Class type) {
        this.type = type;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = SqlParser.toDatabaseName(name);
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public boolean isUpdatable() {
        return updatable;
    }

    public void setUpdatable(boolean updatable) {
        this.updatable = updatable;
    }

    public boolean isInsertable() {
        return insertable;
    }

    public void setInsertable(boolean insertable) {
        this.insertable = insertable;
    }


    public String generateText() {
        if (Database.isPostgresql()) {
            return generatePostgresqlText();
        } else if (Database.isMySQL()) {
            return generateMySqlText();
        }
        throw new RuntimeException("not support database " + Database.getType());
    }

    private String generateMySqlText() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) {
            if (primaryKey && generator.equals(Generator.AUTO)) {
                sb.append(" INT AUTO_INCREMENT");
            } else {
                sb.append(" INT");
            }
        } else if (Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type)) {
            if (primaryKey && generator.equals(Generator.AUTO)) {
                sb.append(" BIGINT AUTO_INCREMENT");
            } else {
                sb.append(" BIGINT");
            }
        } else if (Short.class.isAssignableFrom(type) || short.class.isAssignableFrom(type)) {
            if (primaryKey && generator.equals(Generator.AUTO)) {
                sb.append(" SMALLINT AUTO_INCREMENT");
            } else {
                sb.append(" SMALLINT");
            }
        } else if (Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type)) {
            if (getPrecision() > 0 || getScale() > 0) {
                sb.append(" NUMERIC(" + getPrecision() + ", " + getScale() + ")");
            } else {
                sb.append(" REAL");
            }
        } else if (Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)) {
            if (field.isAnnotationPresent(Money.class)) {
                sb.append(" NUMERIC(10, 2)");
            } else {
                if (getPrecision() > 0 || getScale() > 0) {
                    sb.append(" NUMERIC(" + getPrecision() + ", " + getScale() + ")");
                } else {
                    sb.append(" DOUBLE PRECISION");
                }
            }
        } else if (String.class.isAssignableFrom(type)) {
            if (length > 0) {
                sb.append(" NVARCHAR(" + length + ")");
            } else {
                sb.append(" TEXT");
            }
        } else if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
            sb.append(" BOOLEAN");
        } else if (LocalDateTime.class.isAssignableFrom(getType())) {
            sb.append(" DATETIME");
        } else if (LocalDate.class.isAssignableFrom(getType())) {
            sb.append(" DATE");
        } else if (LocalTime.class.isAssignableFrom(getType())) {
            sb.append(" TIME");
        } else if (Instant.class.isAssignableFrom(getType())) {
            sb.append(" TIMESTAMP");
        } else if (Timestamp.class.isAssignableFrom(getType())) {
            sb.append(" TIMESTAMP");
        } else if (Enum.class.isAssignableFrom(getType())) {
            sb.append(" NVARCHAR(" + length + ")");
        } else {
            throw new RuntimeException("not support" + getType());
        }

        if (isPrimaryKey()) {
            sb.append(", CONSTRAINT pk_" + tableName + " PRIMARY KEY(" + name + ")");
        } else if (!isNullable()) {
            sb.append(" NOT NULL");
        }

        if (comment != null && !comment.isBlank()) {
            sb.append(" COMMENT '").append(comment).append("'");
        }

        return sb.toString();
    }

    private String generatePostgresqlText() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) {
            if (primaryKey && generator.equals(Generator.AUTO)) {
                sb.append(" serial");
            } else {
                sb.append(" int");
            }
        } else if (Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type)) {
            if (primaryKey && generator.equals(Generator.AUTO)) {
                sb.append(" bigserial");
            } else {
                sb.append(" bigint");
            }
        } else if (Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type)) {
            if (getPrecision() > 0 || getScale() > 0) {
                sb.append(" numeric(" + getPrecision() + "," + getScale() + ")");
            } else {
                sb.append(" real");
            }
        } else if (Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)) {
            if (field.isAnnotationPresent(Money.class)) {
                sb.append(" numeric");
            } else {
                if (getPrecision() > 0 || getScale() > 0) {
                    sb.append(" numeric(" + getPrecision() + "," + getScale() + ")");
                } else {
                    sb.append(" double precision");
                }
            }
        } else if (Short.class.isAssignableFrom(type) || short.class.isAssignableFrom(type)) {
            sb.append(" smallint");
        } else if (String.class.isAssignableFrom(type)) {
            sb.append(" text");
        } else if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
            sb.append(" boolean");
        } else if (LocalDateTime.class.isAssignableFrom(getType())) {
            sb.append(" timestamp with time zone");
        } else if (LocalDate.class.isAssignableFrom(getType())) {
            sb.append(" date");
        } else if (LocalTime.class.isAssignableFrom(getType())) {
            sb.append(" time with time zone");
        } else if (Instant.class.isAssignableFrom(getType())) {
            sb.append(" timestamp");
        } else if (Timestamp.class.isAssignableFrom(getType())) {
            sb.append(" timestamp");
        } else if (Enum.class.isAssignableFrom(getType())) {
            sb.append(" text");
        } else if (type.isArray()) {
            if (String.class.isAssignableFrom(type.getComponentType())) {
                sb.append(" text[]");
            }
        } else {
            throw new RuntimeException("not support" + getType());
        }

        if (isPrimaryKey()) {
            sb.append(" constraint pk_" + tableName + " primary key");
        } else if (!isNullable()) {
            sb.append(" not null");
        }

        if (comment != null && !comment.isBlank()) {
            sb.append(" COMMENT '").append(comment).append("'");
        }

        return sb.toString();
    }
}
