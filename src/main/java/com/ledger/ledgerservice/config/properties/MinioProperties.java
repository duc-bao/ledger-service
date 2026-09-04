package com.ledger.ledgerservice.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
public class MinioProperties {
    private String endpoint = "http://localhost:9000";
    private String accessKey = "minio_admin";
    private String secretKey = "minio_password";
    private String bucketName = "ledger-exports";
    private Duration expiryDuration = Duration.ofMinutes(15);
}
