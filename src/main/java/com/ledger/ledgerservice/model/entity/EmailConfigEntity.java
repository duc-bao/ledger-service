package com.ledger.ledgerservice.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ledger.ledgerservice.model.converter.EncryptedStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sys_email_configs")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class EmailConfigEntity extends EntityBase {

    @Column(name = "code", length = 100, nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "provider_type", length = 50, nullable = false)
    @Builder.Default
    private String providerType = "smtp";

    @Column(name = "host", length = 255, nullable = false)
    private String host;

    @Column(name = "port", nullable = false)
    private Integer port;

    @Column(name = "username", length = 255, nullable = false)
    private String username;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "password", length = 500, nullable = false)
    private String password;

    @Column(name = "from_address", nullable = false)
    private String fromAddress;

    @Column(name = "from_name", length = 255, nullable = false)
    private String fromName;

    @Column(name = "reply_to", length = 255)
    private String replyTo;

    @Column(name = "protocol", length = 20)
    @Builder.Default
    private String protocol = "smtp";

    @Column(name = "encoding", nullable = false, length = 50)
    @Builder.Default
    private String encoding = "UTF-8";

    @Column(name = "auth_enabled", nullable = false)
    @Builder.Default
    private Boolean authEnabled = Boolean.TRUE;

    @Column(name = "starttls_enabled", nullable = false)
    @Builder.Default
    private Boolean starttlsEnabled = Boolean.TRUE;

    @Column(name = "ssl_enabled", nullable = false)
    @Builder.Default
    private Boolean sslEnabled = Boolean.FALSE;

    @Column(name = "debug_enabled", nullable = false)
    @Builder.Default
    private Boolean debugEnabled = Boolean.FALSE;

    @Column(name = "timeout_ms")
    private Integer timeoutMs;

    @Column(name = "connection_timeout_ms")
    private Integer connectionTimeoutMs;

    @Column(name = "write_timeout_ms")
    private Integer writeTimeoutMs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_cc", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private List<String> defaultCc = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_bcc", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private List<String> defaultBcc = new ArrayList<>();

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = Boolean.TRUE;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = Boolean.FALSE;
}