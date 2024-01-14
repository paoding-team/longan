package dev.paoding.longan.doc;

import dev.paoding.longan.annotation.Mapping;
import dev.paoding.longan.annotation.RpcService;
import dev.paoding.longan.core.ClassPathBeanScanner;
import dev.paoding.longan.core.ServiceDescriptor;
import dev.paoding.longan.data.Snowflake;
import dev.paoding.longan.util.GsonUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;


public class DocumentService {
    private final static Map<String, MetaNode> metaNodeMap = new HashMap<>();
    /**
     * key为角色名称
     */
    private final static Map<String, MetaNode> rootMap = new HashMap<>();
    private final static Map<String, String> roleMap = new HashMap<>();
    private final static Map<String, MetaMethod> metaMethodMap = new HashMap<>();
    private static String json;
    private static Snowflake snowflake = new Snowflake();

    public static synchronized long nextId() {
        return snowflake.nextId();
    }

    public static void load(Class<?> type) {
        MetaModelService.load();
        List<ServiceDescriptor> classList = ClassPathBeanScanner.getServiceClasses();
        for (ServiceDescriptor serviceDescriptor : classList) {
            loadService(serviceDescriptor);
        }
    }

    public static void check() {
        load();
    }

    public static String load() {
        if (json != null) {
            return json;
        }
        MetaModelService.load();
        List<ServiceDescriptor> classList = ClassPathBeanScanner.getServiceClasses();
        for (ServiceDescriptor serviceDescriptor : classList) {
            if (serviceDescriptor.isAnnotationPresent(RpcService.class)) {
                loadService(serviceDescriptor);
            }
        }

//        bind();

//        Map<String, Object> map = new HashMap<>();
        map.put("roles", roleMap);
        map.put("nodes", rootMap);
        map.put("models", MetaModelService.metaModelIndex);
//        map.put("models", Map.of());
        map.put("methods", metaMethodMap);

        Map<String, String> codeMap = new LinkedHashMap<>();
        codeMap.put("401", "Token错误或失效");
        codeMap.put("403", "没有权限访问");
        codeMap.put("404", "请求的接口或者地址不存在");
        codeMap.put("405", "不支持请求的http method");
        codeMap.put("406", "操作的数据不存在");
        codeMap.put("410", "提交的参数违反约束");
        codeMap.put("411", "提交的参数类型错误");
        codeMap.put("500", "服务器端错误需要联系接口开发人员");

        map.put("codes", codeMap);

        json = GsonUtils.toJson(map);
        return json;
    }

    public static String getModels() {
        return GsonUtils.toJson(map.get("models"));
    }

    public static String getMethods() {
        return GsonUtils.toJson(map.get("methods"));
    }

    public static String getMethod(String methodName) {
        return GsonUtils.toJson(metaMethodMap.get(methodName));
    }

    private static Map<String, Object> map = new HashMap<>();

//    private static Map<String, List<String>> map = new HashMap<>();

//    public void bind(String client, List<String> methodList) {
//        if (map.containsKey(client)) {
//            map.get(client).addAll(methodList);
//        } else {
//            map.put(client, methodList);
//        }
//    }
//
//    private static void bind() {
//        Set<String> clientSet = map.keySet();
//        for (String client : clientSet) {
//            List<String> methodList = map.get(client);
//            for (String method : methodList) {
//                MetaMethod metaMethod = metaMethodMap.get(method);
//                if (metaMethod == null) {
//                    throw new DocumentException("not found '" + method + "' method bind on " + client);
//                }
//                MetaModel metaModel = MetaModelService.getMetaModel(metaMethod.getType());
//                createMetaNode(client, metaModel, metaMethod);
//            }
//        }
//
//    }

    private static void loadService(ServiceDescriptor serviceDescriptor) {
        Class<?> clazz = serviceDescriptor.getServiceClass();
        if (clazz.isAnnotationPresent(RpcService.class)) {
            Class<?> model = (Class<?>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];

            MetaModel metaModel = MetaModelService.getMetaModel(model.getName());

            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Mapping.class)) {
                    MetaMethod metaMethod = MetaMethodService.load(model, method);

                    metaMethodMap.put(metaMethod.getName(), metaMethod);

                    String[] clients = metaMethod.getClients();
                    for (String client : clients) {
                        createMetaNode(client, metaModel, metaMethod);
                    }
                }
            }

        }
    }

    private static void createMetaNode(String client, MetaModel metaModel, MetaMethod metaMethod) {
        String name = null;
        if (client.endsWith("_WEB") || client.endsWith("_APP") || client.endsWith("_ALL")) {
            name = client.substring(0, client.length() - 4);
        } else if (client.endsWith("_MP") || client.endsWith("_H5")) {
            name = client.substring(0, client.length() - 3);
        }
        if (name == null) {
            throw new DocumentException("Expected 'ROLE_NAME_[WEB|APP|MP|H5]' format, '" + client + "' could not be parsed.");
        }

        roleMap.put(client, client);
        MetaNode roleMetaNode = rootMap.get(client);
        if (roleMetaNode == null) {
            roleMetaNode = new MetaNode();
            roleMetaNode.setType("role");
            roleMetaNode.setName(client);

            rootMap.put(client, roleMetaNode);
        }

        String id = client + "#" + metaModel.getSimpleName();
        if (metaNodeMap.containsKey(id)) {
            MetaNode parent = metaNodeMap.get(id);
            MetaNode child = new MetaNode();
            child.setType("method");
            child.setName(metaMethod.getAlias());
            child.setLink(metaMethod.getName());
            parent.addChild(child);
        } else {
            MetaNode parent = new MetaNode();
            parent.setType("model");
            parent.setName(metaModel.getAlias());
            parent.setLink(metaModel.getName());
            roleMetaNode.addChild(parent);

            MetaNode child = new MetaNode();
            child.setType("method");
            child.setName(metaMethod.getAlias());
            child.setLink(metaMethod.getName());
            parent.addChild(child);
            metaNodeMap.put(id, parent);
        }
    }


}
