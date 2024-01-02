package dev.paoding.longan.doc;

import dev.paoding.longan.data.Between;
import dev.paoding.longan.data.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class MetaSampleLoader {

    public static Object load(MetaField metaParam) {
        if (metaParam.isTypeModel()) {
            Map<String, Object> root = new LinkedHashMap<>();
            List<MetaField> children = metaParam.getChildren();
            for (MetaField child : children) {
                if (child.isTypeModel()) {
                    root.put(child.getName(), load(child));
                } else if (child.isActualTypeModel()) {
                    root.put(child.getName(), load(child));
                } else {
                    root.put(child.getName(), child.getSample());
                }
            }
            return root;
        } else if (metaParam.isActualTypeModel()) {
            List<Object> list = new ArrayList<>();
            Map<String, Object> root = new LinkedHashMap<>();
            List<MetaField> children = metaParam.getChildren();
            for (MetaField child : children) {
                if (child.isTypeModel()) {
                    root.put(child.getName(), load(child));
                } else if (child.isActualTypeModel()) {
                    root.put(child.getName(), load(child));
                } else {
                    root.put(child.getName(), child.getSample());
                }
            }
            list.add(root);
            return list;
        } else {
            if (List.class.isAssignableFrom(metaParam.getType())) {
                List<Object> list = new ArrayList<>();
                Class<?> actualType = metaParam.getActualType();
                if (Long.class.isAssignableFrom(actualType) || Integer.class.isAssignableFrom(actualType) || Short.class.isAssignableFrom(actualType)) {
                    list.add(1);
                    list.add(2);
                    list.add(3);
                } else if (Double.class.isAssignableFrom(actualType) || Float.class.isAssignableFrom(actualType)) {
                    list.add(1.1);
                    list.add(2.2);
                    list.add(3.3);
                } else if (CharSequence.class.isAssignableFrom(actualType)) {
                    list.add("a");
                    list.add("b");
                    list.add("c");
                } else if (LocalDate.class.isAssignableFrom(actualType)) {
                    list.add(LocalDate.now());
                } else if (LocalDateTime.class.isAssignableFrom(actualType)) {
                    list.add(LocalDateTime.now());
                } else if (Boolean.class.isAssignableFrom(actualType)) {
                    list.add(true);
                    list.add(false);
                }
                return list;
            } else {
                if (metaParam.getSample() != null) {
                    return metaParam.getSample();
                }
                Class<?> actualType = metaParam.getType();
                if (long.class.isAssignableFrom(actualType) || Long.class.isAssignableFrom(actualType) || int.class.isAssignableFrom(actualType) || Integer.class.isAssignableFrom(actualType) || short.class.isAssignableFrom(actualType) || Short.class.isAssignableFrom(actualType)) {
                    return 1;
                } else if (double.class.isAssignableFrom(actualType) || Double.class.isAssignableFrom(actualType) || float.class.isAssignableFrom(actualType) || Float.class.isAssignableFrom(actualType)) {
                    return 1.1;
                } else if (CharSequence.class.isAssignableFrom(actualType)) {
                    if (metaParam.getName().equals("phone")) {
                        return "13800138000";
                    } else if (metaParam.getName().equals("email")) {
                        return "test@test.com";
                    } else if (metaParam.getName().equals("title")) {
                        return "Java Development Without Spring";
                    }
                    return "a";
                } else if (LocalDate.class.isAssignableFrom(actualType)) {
                    return LocalDate.now();
                } else if (LocalDateTime.class.isAssignableFrom(actualType)) {
                    return LocalDateTime.now();
                } else if (boolean.class.isAssignableFrom(actualType) || Boolean.class.isAssignableFrom(actualType)) {
                    return false;
                } else if (Pageable.class.isAssignableFrom(actualType)) {
                    return metaParam.getSample();
                } else if (Between.class.isAssignableFrom(actualType)) {
                    return metaParam.getSample();
                } else if (void.class.isAssignableFrom(actualType)) {
                    return null;
                }
                return "";
            }
        }
    }
}
