package com.ledger.ledgerservice.model.dto.response.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailConfigResponse {

    private String id;
    private String code;
    private String name;
    private String providerType;
    private String host;
    private Integer port;
    private String username;
    private String fromAddress;
    private String password;
    private String fromName;
    private String replyTo;
    private String protocol;
    private String encoding;
    private Boolean authEnabled;
    private Boolean starttlsEnabled;
    private Boolean sslEnabled;
    private Boolean debugEnabled;
    private Integer timeoutMs;
    private Boolean enabled;
    private Boolean isDefault;
    private Boolean passwordConfigured;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
