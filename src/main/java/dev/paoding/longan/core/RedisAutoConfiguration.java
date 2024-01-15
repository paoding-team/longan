package dev.paoding.longan.core;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;

import java.time.Duration;

public class RedisAutoConfiguration implements ImportBeanDefinitionRegistrar, EnvironmentAware {
    private Environment environment;

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry) {
        RedisConnectionFactory redisConnectionFactory = redisConnectionFactory();
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) registry;
        defaultListableBeanFactory.registerSingleton("redisConnectionFactory", redisConnectionFactory);
        defaultListableBeanFactory.registerSingleton("stringRedisTemplate", stringRedisTemplate(redisConnectionFactory));
    }

    private RedisConnectionFactory redisConnectionFactory() {
        ClientResources clientResources = DefaultClientResources.create();
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(environment.getProperty("longan.redis.host", "127.0.0.1"));
        configuration.setPort(Integer.parseInt(environment.getProperty("longan.redis.port", "6379")));
        configuration.setDatabase(Integer.parseInt(environment.getProperty("longan.redis.database", "0")));
        configuration.setPassword(RedisPassword.of(environment.getProperty("longan.redis.password", "")));

        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(Integer.parseInt(environment.getProperty("longan.redis.pool.total.max", "100")));
        poolConfig.setMinIdle(Integer.parseInt(environment.getProperty("longan.redis.pool.idle.min", "10")));
        poolConfig.setMaxIdle(Integer.parseInt(environment.getProperty("longan.redis.pool.idle.max", "20")));
        poolConfig.setMaxWait(Duration.ofMillis(Integer.parseInt(environment.getProperty("longan.redis.pool.wait.max", "1000"))));

        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder = LettucePoolingClientConfiguration.builder();
        builder.poolConfig(poolConfig);
        builder.commandTimeout(Duration.ofSeconds(Integer.parseInt(environment.getProperty("longan.redis.command.timeout", "10"))));
        builder.clientResources(clientResources);
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(configuration, builder.build());
        connectionFactory.afterPropertiesSet();

        return connectionFactory;
    }


    private StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

}
