package dev.paoding.longan.core;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zaxxer.hikari.HikariDataSource;
import dev.paoding.longan.data.jpa.*;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

public class JdbcAutoConfiguration implements ImportBeanDefinitionRegistrar, EnvironmentAware {
    private Environment environment;

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry) {
        DataSource dataSource = dataSource();
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) registry;
        defaultListableBeanFactory.registerSingleton("dataSource", dataSource);
        defaultListableBeanFactory.registerSingleton("jdbcSession", jdbcSession(dataSource));
        defaultListableBeanFactory.registerSingleton("txManager", txManager(dataSource));
    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    private PlatformTransactionManager txManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    private JdbcSession jdbcSession(DataSource dataSource) {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        JdbcSession jdbcSession = new JdbcSession(namedParameterJdbcTemplate);
        BeanFactory.register(jdbcSession);
        return jdbcSession;
    }

    private DataSource dataSource() {
        boolean showSql = environment.getProperty("longan.datasource.show-sql", Boolean.class, false);
        SqlLogger.showSql(showSql);
        Database.init(environment.getProperty("longan.datasource.url"), environment.getProperty("longan.datasource.username"), environment.getProperty("longan.datasource.password"));
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setThreadFactory(new ThreadFactoryBuilder().setNameFormat("hikari-thread-%d").build());
        hikariDataSource.setMinimumIdle(Integer.parseInt(environment.getProperty("longan.datasource.idle.min", "10")));
        hikariDataSource.setMaximumPoolSize(Integer.parseInt(environment.getProperty("longan.datasource.pool.max", "100")));
        hikariDataSource.setJdbcUrl(environment.getProperty("longan.datasource.url"));
        hikariDataSource.setUsername(environment.getProperty("longan.datasource.username"));
        hikariDataSource.setPassword(environment.getProperty("longan.datasource.password"));
        hikariDataSource.addDataSourceProperty("cachePrepStmts", true);
        hikariDataSource.addDataSourceProperty("prepStmtCacheSize", 250);
        hikariDataSource.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        hikariDataSource.addDataSourceProperty("useServerPrepStmts", true);
        return hikariDataSource;
    }
}
