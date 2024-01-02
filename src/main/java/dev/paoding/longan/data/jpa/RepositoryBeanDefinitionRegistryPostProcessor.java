package dev.paoding.longan.data.jpa;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

//@Component
public class RepositoryBeanDefinitionRegistryPostProcessor implements ApplicationContextAware, EnvironmentAware, BeanFactoryPostProcessor {
    private Environment environment;
    private ApplicationContext context;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
//        boolean showSql = environment.getProperty("jdbc.show-sql", Boolean.class, false);
//        SqlLogger.showSql(showSql);
//
//        String url = environment.getProperty("datasource.url");
//        String username = environment.getProperty("datasource.username");
//        String password = environment.getProperty("datasource.password");
//        DatabasePopulator databasePopulator = new DatabasePopulator(context);
//        databasePopulator.populate(url, username, password);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
