package dev.paoding.longan.doc;

import dev.paoding.longan.data.Between;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class MetaBetween extends MetaParam {

    public Object getSample() {
        Map<String, Object> sample = new HashMap<>();
        if (LocalDate.class.isAssignableFrom(getActualType())) {
            sample.put("start", LocalDate.now().plusWeeks(-1));
            sample.put("end", LocalDate.now());
        } else if (LocalDateTime.class.isAssignableFrom(getActualType())) {
            sample.put("start", LocalDateTime.now().plusWeeks(-1));
            sample.put("end", LocalDateTime.now());
        } else if (Long.class.isAssignableFrom(getActualType()) || Integer.class.isAssignableFrom(getActualType()) || Short.class.isAssignableFrom(getActualType())) {
            sample.put("start", 1);
            sample.put("end", 99);
        } else if (Double.class.isAssignableFrom(getActualType()) || Float.class.isAssignableFrom(getActualType())) {
            sample.put("start", 11.1);
            sample.put("end", 22.3);
        }
        return sample;
    }

    public MetaBetween(Type type, String name,String alias) {
        this.setName(name);
        this.setAlias(alias);
        this.setType(Between.class);
        this.setJsType("Object");
        this.setDartType("Object");


        ParameterizedType parameterizedType = (ParameterizedType) type;
        Class<?> actualType = (Class<?>) parameterizedType.getActualTypeArguments()[0];

//        this.setActualType(actualType);
        this.setTypeIsModel(true);

        if (LocalDate.class.isAssignableFrom(actualType)) {
            MetaAttribute startMetaAttribute = new MetaAttribute();
            startMetaAttribute.setName("start");
            startMetaAttribute.setType(LocalDate.class);
            startMetaAttribute.setAlias("起始日期");
            startMetaAttribute.setSample(LocalDate.now().plusWeeks(-1));
            startMetaAttribute.setDescription("yyyy-MM-dd");
            startMetaAttribute.setNotNull(true);

            MetaAttribute endMetaAttribute = new MetaAttribute();
            endMetaAttribute.setName("end");
            endMetaAttribute.setType(LocalDate.class);
            endMetaAttribute.setAlias("截止日期");
            endMetaAttribute.setSample(LocalDate.now());
            endMetaAttribute.setDescription("yyyy-MM-dd");
            endMetaAttribute.setNotNull(true);

            this.addChild(startMetaAttribute);
            this.addChild(endMetaAttribute);
        } else if (LocalDateTime.class.isAssignableFrom(actualType)) {
            MetaAttribute startMetaAttribute = new MetaAttribute();
            startMetaAttribute.setName("start");
            startMetaAttribute.setType(LocalDateTime.class);
            startMetaAttribute.setAlias("起始时间");
            startMetaAttribute.setSample(LocalDateTime.now().plusWeeks(-1));
            startMetaAttribute.setDescription("yyyy-MM-dd HH:mm:ss");
            startMetaAttribute.setNotNull(true);

            MetaAttribute endMetaAttribute = new MetaAttribute();
            endMetaAttribute.setName("end");
            endMetaAttribute.setType(LocalDateTime.class);
            endMetaAttribute.setAlias("截止时间");
            endMetaAttribute.setSample(LocalDateTime.now());
            endMetaAttribute.setDescription("yyyy-MM-dd HH:mm:ss");
            endMetaAttribute.setNotNull(true);

            this.addChild(startMetaAttribute);
            this.addChild(endMetaAttribute);
        } else if (Long.class.isAssignableFrom(actualType)) {
            MetaAttribute startMetaAttribute = new MetaAttribute();
            startMetaAttribute.setName("start");
            startMetaAttribute.setType(Long.class);
            startMetaAttribute.setAlias("起始数值");
            startMetaAttribute.setSample(1);
            startMetaAttribute.setNotNull(true);

            MetaAttribute endMetaAttribute = new MetaAttribute();
            endMetaAttribute.setName("end");
            endMetaAttribute.setType(Long.class);
            endMetaAttribute.setAlias("截止数值");
            endMetaAttribute.setSample(100);
            endMetaAttribute.setNotNull(true);

            this.addChild(startMetaAttribute);
            this.addChild(endMetaAttribute);
        } else if (Integer.class.isAssignableFrom(actualType)) {
            MetaAttribute startMetaAttribute = new MetaAttribute();
            startMetaAttribute.setName("start");
            startMetaAttribute.setType(Integer.class);
            startMetaAttribute.setAlias("起始数值");
            startMetaAttribute.setSample(1);
            startMetaAttribute.setNotNull(true);

            MetaAttribute endMetaAttribute = new MetaAttribute();
            endMetaAttribute.setName("end");
            endMetaAttribute.setType(Integer.class);
            endMetaAttribute.setAlias("截止数值");
            endMetaAttribute.setSample(100);
            endMetaAttribute.setNotNull(true);

            this.addChild(startMetaAttribute);
            this.addChild(endMetaAttribute);
        } else if (Short.class.isAssignableFrom(actualType)) {
            MetaAttribute startMetaAttribute = new MetaAttribute();
            startMetaAttribute.setName("start");
            startMetaAttribute.setType(Short.class);
            startMetaAttribute.setAlias("起始数值");
            startMetaAttribute.setSample(1);
            startMetaAttribute.setNotNull(true);

            MetaAttribute endMetaAttribute = new MetaAttribute();
            endMetaAttribute.setName("end");
            endMetaAttribute.setType(Short.class);
            endMetaAttribute.setAlias("截止数值");
            endMetaAttribute.setSample(100);
            endMetaAttribute.setNotNull(true);

            this.addChild(startMetaAttribute);
            this.addChild(endMetaAttribute);
        } else if (Double.class.isAssignableFrom(actualType)) {
            MetaAttribute startMetaAttribute = new MetaAttribute();
            startMetaAttribute.setName("start");
            startMetaAttribute.setType(Double.class);
            startMetaAttribute.setAlias("起始数值");
            startMetaAttribute.setSample(10.1);
            startMetaAttribute.setNotNull(true);

            MetaAttribute endMetaAttribute = new MetaAttribute();
            endMetaAttribute.setName("end");
            endMetaAttribute.setType(Double.class);
            endMetaAttribute.setAlias("截止数值");
            endMetaAttribute.setSample(22.3);
            endMetaAttribute.setNotNull(true);

            this.addChild(startMetaAttribute);
            this.addChild(endMetaAttribute);
        } else if (Float.class.isAssignableFrom(actualType)) {
            MetaAttribute startMetaAttribute = new MetaAttribute();
            startMetaAttribute.setName("start");
            startMetaAttribute.setType(Float.class);
            startMetaAttribute.setAlias("起始数值");
            startMetaAttribute.setSample(13.1);
            startMetaAttribute.setNotNull(true);

            MetaAttribute endMetaAttribute = new MetaAttribute();
            endMetaAttribute.setName("end");
            endMetaAttribute.setType(Float.class);
            endMetaAttribute.setAlias("截止数值");
            endMetaAttribute.setSample(18.8);
            endMetaAttribute.setNotNull(true);

            this.addChild(startMetaAttribute);
            this.addChild(endMetaAttribute);
        }

    }
}
