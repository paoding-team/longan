package dev.paoding.longan.data.jpa;

import dev.paoding.longan.core.ClassPathBeanScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

@Slf4j
public class JpaAutoRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry) {
//        ClassPathBeanDefinitionScanner classPathBeanDefinitionScanner = new ClassPathBeanDefinitionScanner(registry,false);
//        classPathBeanDefinitionScanner.addIncludeFilter(new AssignableTypeFilter(Repository.class));
//        for (BeanDefinition candidateComponent : classPathBeanDefinitionScanner.findCandidateComponents("dev")) {
//        }
        log.info("Register jpa repository");
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) registry;
        JdbcSession jdbcSession = defaultListableBeanFactory.getBean(JdbcSession.class);
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


        TableMetaDataManager tableMetaDataManager = TableMetaDataManager.create(jdbcSession);
        tableMetaDataManager.populate();

        registryRepository(registry, Database.getType(), jdbcSession);
    }

    private void registryRepository(BeanDefinitionRegistry registry, String database, JdbcSession jdbcSession) {
        List<Class<?>> repositoryClasses = ClassPathBeanScanner.getRepositoryClasses();
        for (Class<?> repositoryClass : repositoryClasses) {
            if (repositoryClass.isInterface()) {
                if (JpaRepository.class.isAssignableFrom(repositoryClass)) {
                    Type type = ((ParameterizedType) repositoryClass.getGenericInterfaces()[0]).getActualTypeArguments()[0];
                    JpaRepositoryProxy<?, ? extends Serializable> repositoryProxy = new JpaRepositoryProxy<>((Class<?>) type);
                    repositoryProxy.setDatabase(database);
                    repositoryProxy.setJdbcSession(jdbcSession);

                    Object proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{repositoryClass}, repositoryProxy);
                    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(proxy.getClass());
                    builder.addConstructorArgValue(Proxy.getInvocationHandler(proxy));
                    AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
                    beanDefinition.setBeanClass(proxy.getClass());
                    registry.registerBeanDefinition(repositoryClass.getSimpleName(), beanDefinition);
                }
            }
        }
    }


}
