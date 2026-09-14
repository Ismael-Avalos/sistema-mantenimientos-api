package com.umaso.mantenimientos.modules.health.service;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;

public class DatabaseKeepAlive {
    private static final Logger log = LoggerFactory.getLogger(DatabaseKeepAlive.class);
    private final JdbcTemplate jdbc;

    public DatabaseKeepAlive(DataSource dataSource) {
        // Dedicated template: does not change timeouts of business queries.
        jdbc = new JdbcTemplate(dataSource);
        jdbc.setQueryTimeout(5);
    }

    @Scheduled(fixedDelayString = "${app.database-keep-alive.interval:6h}",
            initialDelayString = "${app.database-keep-alive.initial-delay:1m}")
    public void ping() {
        try {
            Integer result = jdbc.queryForObject("SELECT 1", Integer.class);
            if (Integer.valueOf(1).equals(result)) {
                log.info("Database keep-alive succeeded");
            } else {
                log.warn("Database keep-alive failed");
            }
        } catch (DataAccessException exception) {
            // Never include driver messages, URLs, credentials or stack traces here.
            log.warn("Database keep-alive failed");
        }
    }
}
