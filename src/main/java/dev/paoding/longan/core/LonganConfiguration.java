package dev.paoding.longan.core;

import dev.paoding.longan.data.jpa.*;
import org.burningwave.core.assembler.StaticComponentContainer;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
@EnableScheduling
@EnableTransactionManagement
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import({RpcServiceAutoRegistrar.class, JdbcAutoConfiguration.class, JpaAutoRegistrar.class, RedisAutoConfiguration.class})
public class LonganConfiguration {

    static {
        StaticComponentContainer.Modules.exportPackageToAllUnnamed("java.base", "java.lang", "java.time");
    }

    @Bean
    public TaskScheduler scheduledExecutorService() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(8);
        scheduler.setThreadNamePrefix("scheduled-thread-");
        return scheduler;
    }

}
