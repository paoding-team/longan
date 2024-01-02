package dev.paoding.longan.core;

import dev.paoding.longan.annotation.Filter;
import dev.paoding.longan.annotation.Response;
import dev.paoding.longan.data.jpa.BeanProxy;
import org.springframework.beans.BeanUtils;
import org.springframework.cglib.beans.BeanMap;

import java.lang.reflect.Method;
import java.util.*;

public class ResponseFilter {

    public Object filter(Method method, Object result) {
        Response response = method.getAnnotation(Response.class);
        if (response != null) {
            Map<Class<?>, BeanFilter> beanFilterMap = new HashMap<>();
//                BeanFilter beanFilter = new BeanFilter();
//                Type type = method.getGenericReturnType();
//                if (type instanceof ParameterizedType) {
//                    beanFilter.setType((Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0]);
//                } else {
//                    beanFilter.setType((Class<?>) type);
//                }
//                beanFilter.setIncludes(response.includes());
//                beanFilterMap.put(beanFilter.getType(), beanFilter);
            Filter[] filters = response.filters();
            for (Filter filter : filters) {
                beanFilterMap.put(filter.type(), toBeanFilter(filter));
            }

//            filters = response.value();
//            for (Filter filter : filters) {
//                beanFilterMap.put(filter.type(), toBeanFilter(filter));
//            }

            if (Collection.class.isAssignableFrom(result.getClass())) {
                return filterCollection((Collection<?>) result, beanFilterMap);
            } else {
                return filterObject(result, beanFilterMap);
            }
        }
        return result;

//        } catch (IllegalAccessException | InvocationTargetException e) {
//            Throwable throwable = Throwables.getRootCause(e);
//            Class<?> clazz = throwable.getClass();
//            if (CodedException.class.isAssignableFrom(clazz)) {
//                throw (CodedException) throwable;
//            } else if (clazz == EmptyResultDataAccessException.class) {
//                throw new DataNotFoundException("data not found");
//            } else {
////                throw new SystemException(throwable);
//                throw  new ServiceException(throwable);
//            }
//        }
    }

    private BeanFilter toBeanFilter(Filter filter) {
        BeanFilter beanFilter = new BeanFilter();
        beanFilter.setType(filter.type());
        beanFilter.setIncludes(filter.includes());
        return beanFilter;
    }

    private Object filterCollection(Collection<?> collection, Map<Class<?>, BeanFilter> beanFilterMap) {
        List<Object> list = new ArrayList<>();
        for (Object object : collection) {
            list.add(filterObject(object, beanFilterMap));
        }
//        collection.clear();
        return list;
    }

    private Object filterObject(Object object, Map<Class<?>, BeanFilter> beanFilterMap) {
        Class<?> type = object.getClass();
        if (BeanUtils.isSimpleProperty(type)) {
            return object;
        }

        if (object instanceof BeanProxy) {
            type = ((BeanProxy) object).getOriginal().getClass();
        }

        if (beanFilterMap.containsKey(type)) {
            Map<String, Object> resultMap = new HashMap<>();
            Set<String> includes = beanFilterMap.get(type).getIncludes();
            BeanMap beanMap = BeanMap.create(object);
            for (String name : includes) {
                Object value = beanMap.get(name);
                if (value != null) {
                    if (Collection.class.isAssignableFrom(value.getClass())) {
                        resultMap.put(name, filterCollection((Collection<?>) value, beanFilterMap));
                    } else {
                        resultMap.put(name, filterObject(value, beanFilterMap));
                    }
                }
            }


//            Field[] fields = type.getDeclaredFields();
//            BeanMap beanMap = BeanMap.create(object);
//            for (Field field : fields) {
//                String name = field.getName();
//                if (includes.contains(name)) {
//                    Object value = beanMap.get(name);
//                    if (value != null) {
//                        if (Internationalization.isEnabled() && field.isAnnotationPresent(I18n.class)) {
//                            Map<String, String> map = GsonUtils.toLocaleMap(value.toString());
//                            value = map.getOrDefault(Internationalization.getLanguage(), "");
//                            resultMap.put(name, value);
//                        } else {
//                            if (Collection.class.isAssignableFrom(value.getClass())) {
//                                resultMap.put(name, filterCollection((Collection<?>) value, beanFilterMap));
//                            } else {
//                                resultMap.put(name, filterObject(value, beanFilterMap));
//                            }
//                        }
//                    }
//                }
//            }
            return resultMap;
        } else {
            return object;
        }
    }
}
