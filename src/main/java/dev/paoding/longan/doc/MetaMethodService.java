package dev.paoding.longan.doc;

import dev.paoding.longan.annotation.Mapping;
import dev.paoding.longan.util.StringUtils;

import java.lang.reflect.Method;

public class MetaMethodService {

    public static MetaMethod load(Class<?> model, Method method) {
        MetaModel metaModel = MetaModelService.getMetaModel(model.getName());
        Mapping mapping = method.getAnnotation(Mapping.class);
        MetaMethod metaMethod = new MetaMethod();
        if (mapping.alias().isEmpty()) {
            String methodName = method.getName();
            if (methodName.equals("get")) {
                metaMethod.setAlias("获取" + metaModel.getAlias());
            } else if (methodName.equals("save")) {
                metaMethod.setAlias("保存" + metaModel.getAlias());
            } else if (methodName.equals("update")) {
                metaMethod.setAlias("更新" + metaModel.getAlias());
            } else if (methodName.equals("count")) {
                metaMethod.setAlias("统计" + metaModel.getAlias());
            } else if (methodName.equals("delete")) {
                metaMethod.setAlias("删除" + metaModel.getAlias());
            } else if (methodName.equals("find")) {
                metaMethod.setAlias("搜索" + metaModel.getAlias() + "列表");
            } else if (methodName.equals("enable")) {
                metaMethod.setAlias("启用" + metaModel.getAlias());
            } else if (methodName.equals("disable")) {
                metaMethod.setAlias("禁用" + metaModel.getAlias());
            } else if (methodName.equals("batchEnable")) {
                metaMethod.setAlias("批量启用" + metaModel.getAlias());
            } else if (methodName.equals("batchDisable")) {
                metaMethod.setAlias("批量禁用" + metaModel.getAlias());
            }
        } else {
            metaMethod.setAlias(mapping.alias());
        }
        if (metaMethod.getAlias() == null || metaMethod.getAlias().isEmpty()) {
            throw new DocumentException(new DocumentProblem("'alias' of RpcMethod must be not empty.", "method", method));
        }
        metaMethod.setName("/" + StringUtils.lowerFirst(model.getSimpleName()) + "/" + method.getName());
        metaMethod.setType(model.getName());
//        metaMethod.setAnonymous(rpcMethod.freely());
        metaMethod.setDescription(mapping.description());
//        metaMethod.setRole(rpcMethod.roles());
        metaMethod.setClients(mapping.clients());

        metaMethod.setRequest(MetaRequestService.load(model, method));
        metaMethod.setResponse(MetaResponseService.loadFilter(method));


        return metaMethod;
    }
}
