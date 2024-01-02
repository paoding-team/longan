package dev.paoding.longan.doc;

import dev.paoding.longan.annotation.Json;
import dev.paoding.longan.channel.http.MultipartFile;
import dev.paoding.longan.data.jpa.Data;
import dev.paoding.longan.data.Entity;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MetaField {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private String id;
    private String name;
    @Json(serialize = false)
    private Class<?> type;
//    @Json(serialize = false)
    private String javaType;
//    @Json(serialize = false)
    private String dartType;
//    @Json(serialize = false)
    private String jsType;
    @Json(serialize = false)
    private Class<?> actualType;
//    @Json(serialize = false)
    private String actualJavaType;
//    @Json(serialize = false)
    private String actualDartType;
//    @Json(serialize = false)
    private String actualJsType;
    private String alias;
    private Object sample;
    private String description;
//    @Json(serialize = false)
    private boolean isTypeModel;
//    @Json(serialize = false)
    private boolean isActualTypeModel;
    /**
     * 传参是否不允许为 Null
     */
    private boolean notNull;
    @Json(serialize = false)
    private List<MetaField> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getType() {
        return type;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public String getDartType() {
        return dartType;
    }

    public void setDartType(String dartType) {
        this.dartType = dartType;
    }

    public String getJsType() {
        return jsType;
    }

    public void setJsType(String jsType) {
        this.jsType = jsType;
    }

    public Class<?> getActualType() {
        return actualType;
    }

    public String getActualJavaType() {
        return actualJavaType;
    }

    public void setActualJavaType(String actualJavaType) {
        this.actualJavaType = actualJavaType;
    }

    public String getActualDartType() {
        return actualDartType;
    }

    public void setActualDartType(String actualDartType) {
        this.actualDartType = actualDartType;
    }

    public String getActualJsType() {
        return actualJsType;
    }

    public void setActualJsType(String actualJsType) {
        this.actualJsType = actualJsType;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Object getSample() {
        return sample;
    }

    public void setSample(Object sample) {
        if (sample == null) {
            return;
        }
        if (type.equals(sample.getClass())) {
            this.sample = sample;
        } else {
            String text = sample.toString().trim();

            if (Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type)) {
                if (text.isEmpty()) {
                    this.sample = 1L;
                } else {
                    this.sample = Long.valueOf(text);
                }
            } else if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) {
                if (text.isEmpty()) {
                    this.sample = 1;
                } else {
                    this.sample = Integer.valueOf(text);
                }
            } else if (Short.class.isAssignableFrom(type) || short.class.isAssignableFrom(type)) {
                if (text.isEmpty()) {
                    text = "1";
                }
                this.sample = Short.valueOf(text);
            } else if (Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)) {
                if (text.isEmpty()) {
                    this.sample = 99.9d;
                } else {
                    this.sample = Double.valueOf(sample.toString());
                }
            } else if (Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type)) {
                if (text.isEmpty()) {
                    this.sample = 88.1f;
                } else {
                    this.sample = Float.valueOf(sample.toString());
                }
            } else if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
                if (text.isEmpty()) {
                    this.sample = false;
                } else {
                    this.sample = Boolean.valueOf(sample.toString());
                }
            } else if (Instant.class.isAssignableFrom(type)) {
                if (text.isEmpty()) {
                    this.sample = LocalDate.now();
                } else {
                    this.sample = LocalDate.parse(sample.toString());
                }
            } else if (Timestamp.class.isAssignableFrom(type)) {
                if (text.isEmpty()) {
                    this.sample = Timestamp.valueOf(LocalDateTime.now());
                } else {
                    this.sample = LocalDate.parse(sample.toString());
                }
            } else if (LocalDate.class.isAssignableFrom(type)) {
                if (text.isEmpty()) {
                    this.sample = LocalDate.now();
                } else {
                    this.sample = LocalDate.parse(sample.toString());
                }
            } else if (LocalDateTime.class.isAssignableFrom(type)) {
                if (text.isEmpty()) {
                    this.sample = LocalDateTime.now();
                } else {
                    this.sample = LocalDateTime.parse(sample.toString(), dateTimeFormatter);
                }
            } else if (LocalTime.class.isAssignableFrom(type)) {
                if (text.isEmpty()) {
                    this.sample = LocalTime.now();
                } else {
                    this.sample = LocalTime.parse(sample.toString());
                }
            }
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isTypeModel() {
        return isTypeModel;
    }

    public void setTypeIsModel(boolean typeIsModel) {
        this.isTypeModel = typeIsModel;
    }

    public boolean isActualTypeModel() {
        return isActualTypeModel;
    }

    public void setActualTypeIsModel(Boolean actualTypeIsModel) {
        this.isActualTypeModel = actualTypeIsModel;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

//
//    public Boolean getNullable() {
//        return nullable;
//    }
//
//    public void setNullable(Boolean nullable) {
//        this.nullable = nullable;
//    }

    public List<MetaField> getChildren() {
        return children;
    }

    public void setChildren(List<MetaField> children) {
        this.children = children;
    }

    public void addChild(MetaField child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
    }


    public void setActualType(Class<?> actualType) {
        this.actualType = actualType;
        this.actualJavaType = actualType.getName();
        if (CharSequence.class.isAssignableFrom(actualType)) {
            actualDartType = "String";
            actualJsType = "String";
        } else if (Long.class.isAssignableFrom(actualType) || Integer.class.isAssignableFrom(actualType) || Short.class.isAssignableFrom(actualType) ||
                long.class.isAssignableFrom(actualType) || int.class.isAssignableFrom(actualType) || short.class.isAssignableFrom(actualType)) {
            actualDartType = "int";
            actualJsType = "Number";
        } else if (Double.class.isAssignableFrom(actualType) || Float.class.isAssignableFrom(actualType) || double.class.isAssignableFrom(actualType) || float.class.isAssignableFrom(actualType)) {
            actualDartType = "double";
            actualJsType = "Number";
        } else if (Boolean.class.isAssignableFrom(actualType) || boolean.class.isAssignableFrom(actualType)) {
            actualDartType = "bool";
            actualJsType = "boolean";
        } else if (List.class.isAssignableFrom(actualType)) {
            actualDartType = "List";
            actualJsType = "Array";
        } else if (Enum.class.isAssignableFrom(actualType)) {
            actualDartType = "String";
            actualJsType = "String";
        } else if (Instant.class.isAssignableFrom(actualType)) {
            actualDartType = "DateTime";
            actualJsType = "String";
        } else if (Timestamp.class.isAssignableFrom(actualType)) {
            actualDartType = "DateTime";
            actualJsType = "String";
        } else if (LocalDate.class.isAssignableFrom(actualType)) {
            actualDartType = "DateTime";
            actualJsType = "String";
        } else if (LocalDateTime.class.isAssignableFrom(actualType)) {
            actualDartType = "DateTime";
            actualJsType = "String";
        } else if (LocalTime.class.isAssignableFrom(actualType)) {
            actualDartType = "DateTime";
            actualJsType = "String";
        } else if (actualType.isAnnotationPresent(Entity.class) || actualType.isAnnotationPresent(Data.class)) {
            actualDartType = "Object";
            actualJsType = "Object";
            isActualTypeModel = true;
//            if (children == null) {
//                children = new ArrayList<>();
//            }
        }
    }

    public void setType(Class<?> type) {
        this.type = type;
        this.javaType = type.getName();
        if (CharSequence.class.isAssignableFrom(type)) {
            dartType = "String";
            jsType = "String";
        } else if (Long.class.isAssignableFrom(type) || Integer.class.isAssignableFrom(type) || Short.class.isAssignableFrom(type)
                || long.class.isAssignableFrom(type) || int.class.isAssignableFrom(type) || short.class.isAssignableFrom(type)) {
            dartType = "int";
            jsType = "Number";
        } else if (Double.class.isAssignableFrom(type) || Float.class.isAssignableFrom(type) || double.class.isAssignableFrom(type) ||
                float.class.isAssignableFrom(type)) {
            this.dartType = "double";
            this.jsType = "Number";
        } else if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
            dartType = "bool";
            jsType = "boolean";
        } else if (List.class.isAssignableFrom(type)) {
            dartType = "List";
            jsType = "Array";
        } else if (Enum.class.isAssignableFrom(type)) {
            dartType = "String";
            jsType = "String";
        } else if (Instant.class.isAssignableFrom(type)) {
            dartType = "DateTime";
            jsType = "String";
        } else if (Timestamp.class.isAssignableFrom(type)) {
            dartType = "DateTime";
            jsType = "String";
        } else if (LocalDate.class.isAssignableFrom(type)) {
            dartType = "DateTime";
            jsType = "String";
        } else if (LocalDateTime.class.isAssignableFrom(type)) {
            dartType = "DateTime";
            jsType = "String";
        } else if (LocalTime.class.isAssignableFrom(type)) {
            dartType = "DateTime";
            jsType = "String";
        } else if (type.isAnnotationPresent(Entity.class) || type.isAnnotationPresent(Data.class)) {
            dartType = "Object";
            jsType = "Object";
            isTypeModel = true;
//            if (children == null) {
//                children = new ArrayList<>();
//            }
        } else if (Void.class.isAssignableFrom(type) || void.class.isAssignableFrom(type)) {
            dartType = "void";
            jsType = "void";
        } else if (MultipartFile.class.isAssignableFrom(type)) {
            dartType = "MultipartFile";
            jsType = "MultipartFile";
        }
    }

    @Override
    public String toString() {
        return "MetaField{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", javaType='" + javaType + '\'' +
                ", dartType='" + dartType + '\'' +
                ", jsType='" + jsType + '\'' +
                ", actualType=" + actualType +
                ", actualJavaType='" + actualJavaType + '\'' +
                ", actualDartType='" + actualDartType + '\'' +
                ", actualJsType='" + actualJsType + '\'' +
                ", alias='" + alias + '\'' +
                ", sample=" + sample +
                ", description='" + description + '\'' +
                ", isTypeModel=" + isTypeModel +
                ", isActualTypeModel=" + isActualTypeModel +
                ", notNull=" + notNull +
                ", children=" + children +
                '}';
    }
}
