package com.ledger.ledgerservice.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "authentication.jwt")
@Data
public class JwtProperties {
    private String enable;
    private String secret;
    private String key;
    private String keyRefresh;
    private String issuer;
    private Integer timeToLive;
}
