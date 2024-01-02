package dev.paoding.longan.doc;

import dev.paoding.longan.annotation.Param;
import dev.paoding.longan.annotation.Validate;
import dev.paoding.longan.annotation.Request;
import dev.paoding.longan.annotation.Validator;
import dev.paoding.longan.data.Between;
import dev.paoding.longan.data.Entity;
import dev.paoding.longan.data.Pageable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class MetaRequestService {

    public static MetaRequest load(Class<?> model, Method method) {
        MetaRequest metaRequest = new MetaRequest();

        Parameter[] parameters = method.getParameters();
        Map<String, Parameter> parameterMap = new HashMap<>();
        for (Parameter parameter : parameters) {
            parameterMap.put(parameter.getName(), parameter);
        }

        Map<String, MetaValidator> metaValidatorMap = new HashMap<>();
        Map<String, MetaParam> metaParamHashMap = new HashMap<>();
        if (method.isAnnotationPresent(Request.class)) {
            Request request = method.getAnnotation(Request.class);
            Validator[] validators = request.validators();
            Map<String, MetaAttribute> metaAttributeMap = new HashMap<>();
            List<MetaAttribute> metaAttributeList = new ArrayList<>();
            try {
                for (Validator validator : validators) {
                    MetaValidator metaValidator = new MetaValidator();
                    metaValidator.setId(validator.id());
                    metaValidator.setType(validator.type().getName());
                    for (Validate validate : validator.validates()) {
                        MetaAttribute metaAttribute = MetaAttributeService.load(validator.type().getName(), validate);
                        metaAttributeMap.put(validator.type().getName() + validator.id(), metaAttribute);
                        metaValidator.addMetaAttribute(metaAttribute);
                        metaAttributeList.add(metaAttribute);
                    }
                    metaRequest.addMetaValidator(metaValidator);
                    metaValidatorMap.put(validator.type().getName() + validator.id(), metaValidator);
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
                throw new DocumentException(new DocumentProblem(e.getMessage(), method));
            }

            for (MetaAttribute metaAttribute : metaAttributeList) {
                if (metaAttribute.getType() != null) {
                    if (metaAttribute.getType().isAnnotationPresent(Entity.class)) {
                        if (!metaAttributeMap.containsKey(metaAttribute.getType().getName() + metaAttribute.getValidator())) {
                            throw new DocumentException(new DocumentProblem("Not found @Validator that type of " + metaAttribute.getType().getName() + " and id = " + metaAttribute.getValidator(), method));
                        }
                    }
                }
                if (metaAttribute.getActualType() != null) {
                    if (metaAttribute.getActualType().isAnnotationPresent(Entity.class)) {
                        if (!metaAttributeMap.containsKey(metaAttribute.getActualType().getName() + metaAttribute.getValidator())) {
                            throw new DocumentException(new DocumentProblem("Not found @Validator that type of " + metaAttribute.getActualType().getName() + " and id = " + metaAttribute.getValidator(), method));
                        }
                    }
                }
            }

            Param[] params = request.params();
            for (Param param : params) {
                Parameter parameter = parameterMap.get(param.name());
                if (parameter == null) {
                    throw new DocumentException(new DocumentProblem("Parameter '" + param.name() + "' not found in method " + method.getDeclaringClass().getName() + "." + method.getName(), param, method));
                }
                try {
                    MetaParam metaParam = MetaParamService.load(model, method, parameter, param, metaAttributeMap);
                    metaParam.setId(String.valueOf(DocumentService.nextId()));
                    metaParamHashMap.put(metaParam.getName(), metaParam);
                } catch (NotFoundException e) {
                    throw new DocumentException(new DocumentProblem(e.getMessage(), param, method), e);
                }
            }
        } else if (parameters.length > 0) {
            for (Parameter parameter : parameters) {
                if (!Pageable.class.isAssignableFrom(parameter.getType())) {
                    throw new DocumentException(new DocumentProblem("Method '" + method.getDeclaringClass().getName() + "." + method.getName() + "' must be annotated with @Request", method));
                }
            }
        }

        for (Parameter parameter : parameters) {
            String parameterName = parameter.getName();
            if (Pageable.class.isAssignableFrom(parameter.getType())) {
                metaRequest.addMetaParam(new MetaPageable(parameterName));
            } else if (Between.class.isAssignableFrom(parameter.getType())) {
                metaRequest.addMetaParam(new MetaBetween(parameter.getParameterizedType(), parameterName, ""));
            } else {
                if (metaParamHashMap.containsKey(parameterName)) {
                    MetaParam metaParam = metaParamHashMap.get(parameterName);
                    if (Between.class.isAssignableFrom(parameter.getType())) {
                        metaRequest.addMetaParam(new MetaBetween(parameter.getParameterizedType(), parameterName, metaParam.getAlias()));
                    } else {
                        metaRequest.addMetaParam(metaParam);
                    }
                } else {
                    throw new DocumentException(new DocumentProblem("Parameter '" + parameter.getName() + "' must be annotated with @Param", method));
                }
            }
        }

        List<MetaParam> metaParamList = metaRequest.getParams();
        Map<String, Object> map = new LinkedHashMap<>();

        Set<String> loadedParam = new HashSet<>();
        for (MetaParam metaParam : metaParamList) {
//            try {
            loadValidator(loadedParam, metaValidatorMap, metaParam, method);
//            } catch (StackOverflowError e) {
//                throw new DocumentException(new DocumentProblem("Loop recursion @Validator that type of " + metaParam.getType().getName() + " and id = " + metaParam.getValidator(), method));
//            }
            map.put(metaParam.getName(), MetaSampleLoader.load(metaParam));
        }


        metaRequest.setSample(map);

        return metaRequest;
    }

    private static void loadValidator(Set<String> loadedParam, Map<String, MetaValidator> metaValidatorMap, MetaParam parent, Method method) {
        if (parent.isTypeModel()) {
            if (!Pageable.class.isAssignableFrom(parent.getType()) && !Between.class.isAssignableFrom(parent.getType())) {
                String qualifiedName =  parent.getType().getName() + parent.getValidator();
                if(loadedParam.contains(qualifiedName)){
                    parent.setChildren(List.of());
                    return;
                }
                loadedParam.add(qualifiedName);
                MetaValidator metaValidator = metaValidatorMap.get(qualifiedName);
                if (metaValidator == null) {
                    throw new DocumentException(new DocumentProblem("Not found @Validator that type of " + parent.getType().getName() + " and id = " + parent.getValidator(), method));
                }
                loadValidator(loadedParam,metaValidatorMap, parent, metaValidator, method);
            }
        } else if (parent.isActualTypeModel()) {
            String qualifiedName =  parent.getActualType().getName() + parent.getValidator();
            if(loadedParam.contains(qualifiedName)){
                parent.setChildren(List.of());
                return;
            }
            loadedParam.add(qualifiedName);
            MetaValidator metaValidator = metaValidatorMap.get(qualifiedName);
            if (metaValidator == null) {
                throw new DocumentException(new DocumentProblem("Not found @Validator that type of " + parent.getActualType().getName() + " and id = " + parent.getValidator(), method));
            }
            loadValidator(loadedParam,metaValidatorMap, parent, metaValidator, method);
        }

    }


    private static void loadValidator(Set<String> loadedParam, Map<String, MetaValidator> metaValidatorMap, MetaParam parent, MetaValidator metaValidator, Method method) {
        List<MetaAttribute> fields = metaValidator.getFields();
        for (MetaAttribute field : fields) {
            MetaParam metaParam = new MetaParam();
            metaParam.setName(field.getName());
            metaParam.setType(field.getType());
            metaParam.setValidator(field.getValidator());
            if (field.getActualType() != null) {
                metaParam.setActualType(field.getActualType());
            }
            metaParam.setSample(field.getSample());
            parent.addChild(metaParam);

            if (field.isTypeModel() || field.isActualTypeModel()) {
                loadValidator(loadedParam,metaValidatorMap, metaParam, method);
            }
        }
    }
}
