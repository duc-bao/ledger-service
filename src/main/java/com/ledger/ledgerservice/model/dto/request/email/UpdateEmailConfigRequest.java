package com.ledger.ledgerservice.model.dto.request.email;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEmailConfigRequest {

    private String name;

    private String providerType;

    private String host;

    @Min(1)
    @Max(65535)
    private Integer port;

    private String username;

    private String password;

    private String fromAddress;

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
}
