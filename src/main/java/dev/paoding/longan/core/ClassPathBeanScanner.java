package dev.paoding.longan.core;

import dev.paoding.longan.annotation.RpcService;
import dev.paoding.longan.channel.http.HandlerInterceptor;
import dev.paoding.longan.data.Entity;
import dev.paoding.longan.data.jpa.JpaRepositoryProxy;
import dev.paoding.longan.data.jpa.MetaTableFactory;
import dev.paoding.longan.data.jpa.JpaRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ClassPathBeanScanner {
    private final static List<Class<?>> allEntityClasses = new ArrayList<>();
    private final static List<Class<?>> projectEntityClasses = new ArrayList<>();
    private final static List<Class<?>> repositoryClasses = new ArrayList<>();
    private final static List<ServiceDescriptor> serviceClasses = new ArrayList<>();
    private static Class<?> handlerInterceptor;

    public ClassPathBeanScanner(Set<String> candidatePackages) {
        scan(candidatePackages);
    }

    private void scan(Set<String> candidatePackages) {
        SortedSet<String> basePackages = new TreeSet<>();
        for (String candidatePackage : candidatePackages) {
            boolean repeated = false;
            for (String basePackage : basePackages) {
                if (candidatePackage.startsWith(basePackage + ".")) {
                    repeated = true;
                    break;
                }
            }
            if (!repeated) {
                basePackages.add(candidatePackage);
            }
        }

        ClassLoader classLoader = ClassPathBeanScanner.class.getClassLoader();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(classLoader);
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        Set<String> moduleCache = new HashSet<>();
        Set<String> allCache = new HashSet<>();
        try {
            for (String basePackage : basePackages) {
                String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + basePackage.replaceAll("\\.", "/") + "/**/*.class";
                Resource[] resources = resolver.getResources(pattern);
                for (Resource resource : resources) {
                    Class<?> clazz = loadClass(classLoader, metadataReaderFactory, resource);
                    if (clazz == null) {
                        continue;
                    }
                    if (!allCache.contains(clazz.getName())) {
                        allCache.add(clazz.getName());
                        if (clazz.isAnnotationPresent(Entity.class)) {
                            allEntityClasses.add(clazz);
                        }
                    }
                    if (!moduleCache.contains(clazz.getName())) {
                        moduleCache.add(clazz.getName());
                        if (JpaRepository.class.isAssignableFrom(clazz) && JpaRepository.class != clazz && JpaRepositoryProxy.class != clazz) {
                            Type type = ((ParameterizedType) clazz.getGenericInterfaces()[0]).getActualTypeArguments()[0];
                            Class<?> modelClass = (Class<?>) type;
                            if (modelClass.isAnnotationPresent(Entity.class)) {
                                projectEntityClasses.add(modelClass);
                                repositoryClasses.add(clazz);
                            }
                        } else if (clazz.isAnnotationPresent(RpcService.class)) {
                            Type genericSuperclass = clazz.getGenericSuperclass();
                            if (genericSuperclass instanceof ParameterizedType parameterizedType) {
                                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                                for (Type actualTypeArgument : actualTypeArguments) {
                                    Class<?> modelClass = (Class<?>) actualTypeArgument;
                                    if (modelClass.isAnnotationPresent(Entity.class)) {
                                        serviceClasses.add(new ServiceDescriptor(clazz));
                                        break;
                                    }
                                }
                            }
                        } else if (HandlerInterceptor.class.isAssignableFrom(clazz)) {
                            handlerInterceptor = clazz;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MetaTableFactory.load();
    }

    public static Class<?> getHandlerInterceptor() {
        return handlerInterceptor;
    }

    public static List<Class<?>> getProjectEntityClasses() {
        return projectEntityClasses;
    }

    public static List<Class<?>> getAllEntityClasses() {
        return allEntityClasses;
    }

    public static List<Class<?>> getRepositoryClasses() {
        return repositoryClasses;
    }

    public static List<ServiceDescriptor> getServiceClasses() {
        return serviceClasses;
    }

    private static Class<?> loadClass(ClassLoader loader, MetadataReaderFactory readerFactory, Resource resource) {
        try {
            MetadataReader reader = readerFactory.getMetadataReader(resource);
            String name = reader.getClassMetadata().getClassName();
            return Class.forName(name, false, loader);
        } catch (NoClassDefFoundError | IOException | ClassNotFoundException e) {
            return null;
        }
    }
}
