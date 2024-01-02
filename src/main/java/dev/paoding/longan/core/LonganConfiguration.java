package dev.paoding.longan.core;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zaxxer.hikari.HikariDataSource;
import dev.paoding.longan.data.*;
import dev.paoding.longan.data.jpa.*;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.burningwave.core.assembler.StaticComponentContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 参考 https://blog.csdn.net/m0_71777195/article/details/131303029 实现多数据源切换和事务管理
 */
@Configuration
@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
@EnableScheduling
@EnableTransactionManagement
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import(LonganRegistrar.class)
public class LonganConfiguration {
    private final Logger logger = LoggerFactory.getLogger(LonganConfiguration.class);
    @Resource
    private Environment environment;

    static {
        StaticComponentContainer.Modules.exportPackageToAllUnnamed("java.base", "java.lang", "java.time");
    }

    @Bean
    public JdbcSession JdbcSession(RepositoryPostProcessor repositoryPostProcessor) {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource());
        JdbcSession jdbcSession = new JdbcSession(namedParameterJdbcTemplate);
        repositoryPostProcessor.postProcessAfterInitialization(jdbcSession);
        BeanFactory.register(jdbcSession);
        return jdbcSession;
    }

    @Bean
    public DataSource dataSource() {
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

    @Bean
    public PlatformTransactionManager txManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean(destroyMethod = "shutdown")
    public ClientResources lettuceClientResources() {
        return DefaultClientResources.create();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(ClientResources clientResources) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(environment.getProperty("longan.redis.host", "127.0.0.1"));
        configuration.setPort(Integer.parseInt(environment.getProperty("longan.redis.port", "6379")));
        configuration.setDatabase(Integer.parseInt(environment.getProperty("longan.redis.database", "0")));
        configuration.setPassword(RedisPassword.of(environment.getProperty("longan.redis.password", "")));

        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(Integer.parseInt(environment.getProperty("longan.redis.pool.total.max", "100")));
        poolConfig.setMinIdle(Integer.parseInt(environment.getProperty("longan.redis.pool.idle.min", "10")));
        poolConfig.setMaxIdle(Integer.parseInt(environment.getProperty("longan.redis.pool.idle.max", "20")));
        poolConfig.setMaxWaitMillis(Integer.parseInt(environment.getProperty("longan.redis.pool.wait.max", "1000")));

        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder = LettucePoolingClientConfiguration.builder();
        builder.poolConfig(poolConfig);
        builder.commandTimeout(Duration.ofSeconds(Integer.parseInt(environment.getProperty("longan.redis.command.timout", "10"))));
        builder.clientResources(clientResources);
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(configuration, builder.build());
        connectionFactory.afterPropertiesSet();

        return connectionFactory;
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    public static RepositoryPostProcessor repositoryPostProcessor() {
        return new RepositoryPostProcessor() {
            private final List<JpaRepositoryProxy<?, ?>> repositoryProxyList = new ArrayList<>();

            @Override
            public void addRepositoryProxy(JpaRepositoryProxy<?, ?> repositoryProxy) {
                repositoryProxyList.add(repositoryProxy);
            }

            @Override
            public void postProcessAfterInitialization(JdbcSession jdbcSession) {
                for (JpaRepositoryProxy<?, ?> repositoryProxy : repositoryProxyList) {
                    repositoryProxy.setJdbcSession(jdbcSession);
                }
            }
        };
    }

    @Bean
    public TaskScheduler scheduledExecutorService() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(8);
        scheduler.setThreadNamePrefix("scheduled-thread-");
        return scheduler;
    }

}
