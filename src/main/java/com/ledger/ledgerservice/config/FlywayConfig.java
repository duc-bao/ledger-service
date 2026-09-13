package com.ledger.ledgerservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            try {
                log.info("Executing Flyway repair to synchronize migration checksums in local environment...");
                flyway.repair();
            } catch (Exception e) {
                log.warn("Flyway repair encountered an issue: {}", e.getMessage());
            }
            log.info("Executing Flyway migrate...");
            flyway.migrate();
        };
    }
}
