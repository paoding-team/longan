package dev.paoding.longan.data.jpa;

import dev.paoding.longan.core.ClassPathBeanScanner;
import dev.paoding.longan.data.Entity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MetaTableFactory {
    private static final Map<String, MetaTable<?>> cache = new ConcurrentHashMap<>();
//    private static final Map<String, Map<String, ManyToManyPoint>> manyToManyMap = new HashMap<>();
//    private static final Map<String, Map<String, OneToManyPoint>> oneToManyMap = new HashMap<>();


    public static <T> MetaTable<T> get(Class<T> clazz) {
        String name = getName(clazz);
        return (MetaTable<T>) cache.get(name);
    }

    public static <T> MetaTable<T> get(T bean) {
        String name = getName(bean.getClass());
        return (MetaTable<T>) cache.get(name);
    }

    private static <T> MetaTable<T> create(Class<T> clazz) {
        String name = getName(clazz);
        MetaTable<T> metaTable = new MetaTable<>(clazz);
//        if (manyToManyMap.containsKey(name)) {
//            Map<String, ManyToManyPoint> manyToManyPointMap = manyToManyMap.get(name);
//            Set<String> keySet = manyToManyPointMap.keySet();
//            for (String key : keySet) {
//                ManyToManyPoint manyToManyPoint = manyToManyPointMap.get(key);
//                metaTable.addManyToMany(key, null, manyToManyPoint, manyToManyPoint.getSlaver());
//            }
//        }
        cache.put(name, metaTable);
        return metaTable;
    }

    private static String getName(Class<?> clazz) {
        String name = clazz.getName();
        int i = name.indexOf("$");
        if (i > 0) {
            name = name.substring(0, i);
        }
        return name;
    }

//    public static void addOneToMany(Class master, Class slaver,String joinField) {
//        String name = StringUtils.lowerFirst(slaver.getSimpleName());
//        MetaTable metaTable = cache.get(getName(master));
//        if (metaTable == null) {
//            OneToManyPoint oneToManyPoint = new OneToManyPoint(master, slaver);
//            oneToManyPoint.setJoinField(joinField);
//            if(oneToManyMap.containsKey(getName(master))){
//                oneToManyMap.get(getName(master)).put(joinField,oneToManyPoint);
//            }else {
//                Map<String, OneToManyPoint> oneToManyPointMap = new HashMap<>();
//                oneToManyPointMap.put(joinField,oneToManyPoint);
//            }
//        }else {
//            if(metaTable.getOneToManyPoint(joinField) == null){
//                metaTable.addManyToMany();
//            }
//        }
//    }

//    public static void addManyToMany(Class master, Class slaver, String role) {
//        String name = StringUtils.lowerFirst(slaver.getSimpleName());
//        MetaTable metaTable = cache.get(getName(master));
//        if (metaTable == null) {
//            ManyToManyPoint manyToManyPoint = new ManyToManyPoint(master, slaver);
//            if (!role.isEmpty()) {
//                manyToManyPoint.addRole(role);
//                name = role;
//            }
//            if (manyToManyMap.containsKey(getName(master))) {
//                manyToManyMap.get(getName(master)).put(name, manyToManyPoint);
//            }else {
//                Map<String, ManyToManyPoint> manyToManyPointMap = new HashMap<>();
//                manyToManyPointMap.put(name,manyToManyPoint);
//                manyToManyMap.put(getName(master),manyToManyPointMap);
//            }
//        } else {
//            if (!role.isEmpty()) {
//                name = role;
//            }
//            if (metaTable.getManyToManyPoint(name) == null) {
//                ManyToManyPoint manyToManyPoint = new ManyToManyPoint(master, slaver);
//                if (!role.isEmpty()) {
//                    manyToManyPoint.addRole(role);
//                }
//                metaTable.addManyToMany(name, null, manyToManyPoint, slaver);
//            }
//        }
//    }


    public static void load( ) {
        List<Class<?>> entityList = ClassPathBeanScanner.getModuleEntityClassList();
        for (Class<?> classType : entityList) {
            if (classType.isAnnotationPresent(Entity.class)) {
                MetaTableFactory.create(classType);
            }
        }
    }
}
