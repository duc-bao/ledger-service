package com.ledger.ledgerservice.model.dto.request.email;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CreateEmailConfigRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @Builder.Default
    private String providerType = "smtp";

    @NotBlank
    private String host;

    @NotNull
    @Min(1)
    @Max(65535)
    private Integer port;

    @NotBlank
    private String username;

    @NotBlank
    @ToString.Exclude
    private String password;

    @NotBlank
    private String fromAddress;

    @NotBlank
    private String fromName;

    private String replyTo;

    @Builder.Default
    private String protocol = "smtp";

    @Builder.Default
    private String encoding = "UTF-8";

    @Builder.Default
    private Boolean authEnabled = Boolean.TRUE;

    @Builder.Default
    private Boolean starttlsEnabled = Boolean.TRUE;

    @Builder.Default
    private Boolean sslEnabled = Boolean.FALSE;

    @Builder.Default
    private Boolean debugEnabled = Boolean.FALSE;

    private Integer timeoutMs;
}
