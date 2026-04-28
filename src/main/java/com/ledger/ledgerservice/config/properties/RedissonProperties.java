package com.ledger.ledgerservice.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "redisson")
@Configuration
public class RedissonProperties {
    private String host;
    private int port;
    private String password;
    private int database;
    private int timeout;
}
