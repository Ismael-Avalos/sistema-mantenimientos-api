package com.umaso.mantenimientos.config;

import com.umaso.mantenimientos.modules.health.service.DatabaseKeepAlive;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(name = "app.database-keep-alive.enabled", havingValue = "true")
public class DatabaseKeepAliveConfig {
    @Bean
    DatabaseKeepAlive databaseKeepAlive(DataSource dataSource) {
        return new DatabaseKeepAlive(dataSource);
    }
}
