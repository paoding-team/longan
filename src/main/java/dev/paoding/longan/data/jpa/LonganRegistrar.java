package dev.paoding.longan.data.jpa;

import dev.paoding.longan.channel.http.DefaultHandlerInterceptor;
import dev.paoding.longan.channel.http.HandlerInterceptor;
import dev.paoding.longan.core.ClassPathBeanScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class LonganRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware {
    private final Logger logger = LoggerFactory.getLogger(LonganRegistrar.class);
    private BeanFactory beanFactory;

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry) {
//        ClassPathBeanDefinitionScanner classPathBeanDefinitionScanner = new ClassPathBeanDefinitionScanner(registry,false);
//        classPathBeanDefinitionScanner.addIncludeFilter(new AssignableTypeFilter(Repository.class));
//        for (BeanDefinition candidateComponent : classPathBeanDefinitionScanner.findCandidateComponents("dev")) {
//        }
        logger.info("register jpa repository");
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
//        defaultListableBeanFactory.addBeanPostProcessor(new BeanPostProcessor() {
//            @Override
//            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
//                return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
//            }
//
//            @Override
//            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//                return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
//            }
//        });
//        for (String beanDefinitionName : defaultListableBeanFactory.getBeanDefinitionNames()) {
//            BeanDefinition beanDefinition = defaultListableBeanFactory.getBeanDefinition(beanDefinitionName);
//            String className = beanDefinition.getBeanClassName();
//            if (className != null) {
//                try {
//                    Class<?> beanClass = ClassUtils.forName(className, LonganRegistrar.class.getClassLoader());
//                    if (AnnotationUtils.isCandidateClass(beanClass, Component.class)) {
//                    }
//                } catch (ClassNotFoundException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//        defaultListableBeanFactory.getBeansWithAnnotation(Component.class)
//        String[] candidateNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(defaultListableBeanFactory, HandlerInterceptor.class);


        SortedSet<String> candidatePackages = new TreeSet<>();
        String[] beanDefinitionNames = defaultListableBeanFactory.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = defaultListableBeanFactory.getBeanDefinition(beanDefinitionName);
            if (beanDefinition instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
                Map<String, Object> annotationAttributeMap = annotatedBeanDefinition.getMetadata().getAnnotationAttributes(ComponentScan.class.getName());
                if (annotationAttributeMap != null && beanDefinition.getBeanClassName() != null) {
                    candidatePackages.addAll(Arrays.asList((String[]) annotationAttributeMap.get("basePackages")));
                    candidatePackages.add(ClassUtils.getPackageName(beanDefinition.getBeanClassName()));
                }
            }
        }

        ClassPathBeanScanner classPathBeanScanner = new ClassPathBeanScanner(candidatePackages);
        defaultListableBeanFactory.registerSingleton(ClassPathBeanScanner.class.getSimpleName(), classPathBeanScanner);

        if (ClassPathBeanScanner.getHandlerInterceptor() == null) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(HandlerInterceptor.class);
            AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
            beanDefinition.setBeanClass(DefaultHandlerInterceptor.class);
            registry.registerBeanDefinition(HandlerInterceptor.class.getSimpleName(), beanDefinition);
        }


        RepositoryPostProcessor repositoryPostProcessor = beanFactory.getBean(RepositoryPostProcessor.class);
        registryRepository(registry, repositoryPostProcessor, Database.getType());
    }

    private void registryRepository(BeanDefinitionRegistry registry, RepositoryPostProcessor repositoryPostProcessor, String database) {
        List<Class<?>> repositoryClasses = ClassPathBeanScanner.getRepositoryClassList();
        for (Class<?> repositoryClass : repositoryClasses) {
            if (repositoryClass.isInterface()) {
                if (JpaRepository.class.isAssignableFrom(repositoryClass)) {
                    Type type = ((ParameterizedType) repositoryClass.getGenericInterfaces()[0]).getActualTypeArguments()[0];
                    JpaRepositoryProxy<?, ? extends Serializable> repositoryProxy = new JpaRepositoryProxy<>((Class<?>) type);
                    repositoryProxy.setDatabase(database);
                    repositoryPostProcessor.addRepositoryProxy(repositoryProxy);

                    Object proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(),
                            new Class[]{repositoryClass}, repositoryProxy);
                    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(proxy.getClass());
                    builder.addConstructorArgValue(Proxy.getInvocationHandler(proxy));
                    AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
                    beanDefinition.setBeanClass(proxy.getClass());
                    registry.registerBeanDefinition(repositoryClass.getSimpleName(), beanDefinition);
                }
            }
        }
    }

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

}
