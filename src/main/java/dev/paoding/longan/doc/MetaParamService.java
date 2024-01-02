package dev.paoding.longan.doc;

import dev.paoding.longan.annotation.Param;
import dev.paoding.longan.data.Entity;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

public class MetaParamService {
    public static MetaParam load(Class<?> model, Method method, Parameter parameter, Param param, Map<String, MetaAttribute> metaAttributeMap) throws NotFoundException {
        Type type = parameter.getParameterizedType();
        String modelName = null;

        MetaParam metaParam = new MetaParam();
        metaParam.setType(parameter.getType());
        metaParam.setJavaType(parameter.getType().getName());
        metaParam.setName(param.name());
        metaParam.setAlias(param.alias());
        metaParam.setSample(param.example());
        metaParam.setDescription(param.description());

        if (param.notNull()) {
            metaParam.setNotNull(true);
        }
        if (param.notBlank()) {
            metaParam.setNotBlank(true);
        }
        if (param.notEmpty()) {
            metaParam.setNotEmpty(true);
        }
        if (param.size().length == 2) {
            metaParam.setSize(param.size());
        }
        if (!param.regexp().isEmpty()) {
            metaParam.setRegexp(param.regexp());
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class<?> actualType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            if (actualType.isAnnotationPresent(Entity.class)) {
                metaParam.setActualTypeIsModel(true);
                metaParam.setValidator(param.validator());

//                loadAttribute(method, param, metaParam, modelName);
//                metaParam.addChild(metaAttributeMap.get(modelName));

                if (param.alias().isEmpty()) {
                    metaParam.setAlias(MetaModelService.metaModelIndex.get(actualType.getName()).getAlias() + "集合");
                }
            }
            metaParam.setActualType(actualType);


        } else if (parameter.getType().isAnnotationPresent(Entity.class)) {
            metaParam.setValidator(param.validator());
            modelName = parameter.getType().getName();
            metaParam.setTypeIsModel(true);
//            loadAttribute(method, param, metaParam, modelName);
//            metaParam.addChild(metaAttributeMap.get(modelName));

            if (param.alias().isEmpty()) {
                metaParam.setAlias(MetaModelService.metaModelIndex.get(parameter.getType().getName()).getAlias());
            }
        }

        if (metaParam.getAlias().isEmpty()) {
            String alias = MetaModelService.getMetaFieldAlias(model.getName() + "." + param.name());

            if (!alias.isEmpty()) {
                metaParam.setAlias(alias);
            } else {
                throw new DocumentException(new DocumentProblem("alias of @Param must be not empty ", param, method));
            }
        }

//        Attribute[] attributes = param.attribute();
//        for (Attribute attribute : attributes) {
//            metaParam.addChild(MetaAttributeService.load(modelName, attribute));
//        }

        return metaParam;
    }

//    private static void loadAttribute(Method method, Param param, MetaParam metaParam, String modelName) throws NotFoundException {
//        Prop[] props = param.props();
//        if (param.exists()) {
//            metaParam.setNotNull(true);
//            MetaAttribute metaAttribute = new MetaAttribute();
//            metaAttribute.setName("id");
//            metaAttribute.setNotNull(true);
//
//            MetaField originalMetaField = MetaModelService.getMetaField(modelName + ".id");
//            if (originalMetaField == null) {
//                throw new NotFoundException("Field 'id' was not found in class " + modelName).append(originalMetaField);
//            }
//            metaAttribute.setType(originalMetaField.getType());
//            metaAttribute.setAlias(originalMetaField.getAlias());
//            metaAttribute.setSample(originalMetaField.getSample());
//            metaAttribute.setDescription(originalMetaField.getDescription());
//            metaAttribute.setJavaType(originalMetaField.getJavaType());
//            metaAttribute.setDartType(originalMetaField.getDartType());
//            metaAttribute.setJsType(originalMetaField.getJsType());
//            metaAttribute.setTypeIsModel(originalMetaField.isTypeIsModel());
//            metaParam.addChild(metaAttribute);
//            return;
//        }
//        if (props.length == 0) {
//            throw new DocumentException(new DocumentProblem("Expected at least 1 @Prop.", "Request", param, method));
//        }
//        for (Prop prop : props) {
//            metaParam.addChild(MetaAttributeService.load(modelName, prop));
//        }
//    }
}
