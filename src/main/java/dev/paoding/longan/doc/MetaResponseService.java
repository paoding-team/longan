package dev.paoding.longan.doc;

import dev.paoding.longan.annotation.*;
import dev.paoding.longan.data.Entity;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class MetaResponseService {
    public static MetaResponse loadFilter(Method method) {
        MetaResponse metaResponse = new MetaResponse();

        Set<String> loadedFilters = new HashSet<>();
        Map<String, MetaFilter> metaFilterMap = new HashMap<>();
        MetaParam metaParam = new MetaParam();
        if (method.isAnnotationPresent(Response.class)) {
            Response response = method.getAnnotation(Response.class);
//            metaParam.setAlias(response.alias());
            metaParam.setDescription(response.description());

            List<Filter> filters = new ArrayList<>();
            Collections.addAll(filters, response.filters());
//            Collections.addAll(filters, response.value());
            for (Filter filter : filters) {
                if (filter.includes().length == 0) {
                    throw new DocumentException(new DocumentProblem("Filter '" + filter.type().getSimpleName() + "' include must not be empty", "Response", method));
                }

                MetaFilter metaFilter = new MetaFilter();
                metaFilter.setType(filter.type().getName());
                metaFilter.setIncludes(Arrays.asList(filter.includes()));

                for (String name : filter.includes()) {
                    MetaField metaField = MetaModelService.getMetaField(filter.type().getName() + "." + name);
                    if (metaField == null) {
                        throw new DocumentException(new DocumentProblem("Field '" + name + "' was not found in class " + filter.type().getName(), "Response", method));
                    }
                    metaField.setId(String.valueOf(DocumentService.nextId()));
                    metaFilter.addMetaField(metaField);
                }

                metaResponse.addMetaFilter(metaFilter);

                metaFilterMap.put(filter.type().getName(), metaFilter);

            }

            Type type = method.getGenericReturnType();
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                metaParam.setType((Class<?>) parameterizedType.getRawType());
                Class<?> actualType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                metaParam.setActualType(actualType);
                if (actualType.isAnnotationPresent(Entity.class)) {
//                    if (response.filters().length == 0 && response.value().length == 0) {
                    if (response.filters().length == 0 ) {
                        throw new DocumentException(new DocumentProblem("Response 'filters' must be not empty at the same time", "Response", method));
                    }
                    loadFilter(loadedFilters,metaParam, metaFilterMap,  actualType,method);
                }
            } else {
                metaParam.setType(method.getReturnType());
                if (method.getReturnType().isAnnotationPresent(Entity.class)) {
//                    if (response.filters().length == 0 && response.value().length == 0) {
                    if (response.filters().length == 0) {
                        throw new DocumentException(new DocumentProblem("Response 'value' and 'filter' must be not empty at the same time", "Response", method));
                    }
                    loadFilter(loadedFilters,metaParam, metaFilterMap,  method.getReturnType(),method);
//                    getSample(map,metaFilterMap,method.getReturnType(),method);
//                    metaParam.setSample(map);
                } else {
                    if (response.filters().length > 0) {
                        throw new DocumentException(new DocumentProblem("Response 'filter' is unused and has no side effects.", "Response", method));
                    }
                    metaParam.setSample(response.sample());
                }
            }
//                metaParam.setSample(response.sample());
//            if (metaParam.getAlias().isEmpty()) {
//                throw new DocumentException(new DocumentProblem("'alias' of Response must be not empty ", "Response", method));
//            }
        } else {
            if (void.class.isAssignableFrom(method.getReturnType())) {
                metaParam.setType(void.class);
            } else {
                throw new DocumentException(new DocumentProblem("Method '" + method.getDeclaringClass().getName() + "." + method.getName() + "' must be annotated with @Response ", "Response", method));
            }
        }

//        if (metaFilterMap.size() > 0) {
//            for (MetaFilter metaFilter : metaFilterMap.values()) {
//                throw new DocumentException(new DocumentProblem("Filter '" + metaFilter.getType().getSimpleName() + "' is unused and has no side effects.", "Response", method));
//            }
//
//        }

        metaParam.setId(String.valueOf(DocumentService.nextId()));
        metaResponse.setParam(metaParam);
        metaResponse.setSample(MetaSampleLoader.load(metaParam));
        return metaResponse;
    }



    /**
     * 加载Filter
     *
     * @param parent
     * @param metaFilterMap
     * @param type
     */
    private static void loadFilter(Set<String> loadedFilters, MetaField parent, Map<String, MetaFilter> metaFilterMap,Class<?> type,Method method) {
        if(loadedFilters.contains(type.getName())){
            parent.setChildren(List.of());
            return;
        }
        loadedFilters.add(type.getName());
        if (!metaFilterMap.containsKey(type.getName())) {
            throw new DocumentException(new DocumentProblem("Filter '" + type.getSimpleName() + "' was not found ", "Response",method ));
        }
//        if(parent.getAlias() == null||parent.getAlias().isEmpty()) {
//            parent.setAlias(MetaModelService.getMetaModelAlias(type.getName()));
//        }

        MetaFilter metaFilter = metaFilterMap.get(type.getName());
//        metaFilterMap.remove(type.getName());
        List<String> include = metaFilter.getIncludes();

        for (String name : include) {
            MetaField metaField = MetaModelService.getMetaField(type.getName() + "." + name);
            if (metaField == null) {
                throw new DocumentException(new DocumentProblem("Field '" + name + "' was not found in class " + type.getName(), "Response",method ));
            }
            if (metaField.isTypeModel()) {
                MetaField child = new MetaField();
                child.setName(metaField.getName());
                child.setType(metaField.getType());
                if (metaField.getAlias().isEmpty()) {
                    child.setAlias(MetaModelService.getMetaModelAlias(metaField.getType().getName()));
                } else {
                    child.setAlias(metaField.getAlias());
                }
                loadFilter(loadedFilters, child, metaFilterMap,  metaField.getType(),method);
                parent.addChild(child);
            } else if (metaField.isActualTypeModel()) {
                MetaField child = new MetaField();
                child.setName(metaField.getName());
                child.setType(metaField.getType());
                if (metaField.getAlias().isEmpty()) {
                    child.setAlias(MetaModelService.getMetaModelAlias(metaField.getActualType().getName()) + "集合");
                } else {
                    child.setAlias(metaField.getAlias());
                }
                child.setActualType(metaField.getActualType());
                loadFilter(loadedFilters, child, metaFilterMap,  metaField.getActualType(),method);
                parent.addChild(child);
            } else {
                parent.addChild(metaField);
            }
        }

    }
}
