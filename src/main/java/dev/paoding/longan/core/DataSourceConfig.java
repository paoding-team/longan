package dev.paoding.longan.core;

import com.zaxxer.hikari.HikariConfig;
import org.springframework.core.env.Environment;

public class DataSourceConfig extends HikariConfig {
    private Environment environment;

    public DataSourceConfig(Environment environment) {
        this.environment = environment;
    }


}
