package com.ledger.ledgerservice.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ledger.ledgerservice.model.enums.PermissionMatchType;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import lombok.Builder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "idp_permission_apis", indexes = {
        @Index(columnList = "service_code, http_method, status"),
        @Index(columnList = "uri_pattern")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PermissionApi extends EntityBase {
    @Column(name = "permission_id", nullable = false)
    private String permissionId;

    @Column(name = "http_method", length = 10, nullable = false)
    private String httpMethod;

    @Column(name = "uri_pattern", length = 300, nullable = false)
    private String uriPattern;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", length = 20, nullable = false)
    @Builder.Default
    private PermissionMatchType matchType = PermissionMatchType.EXACT;

    @Column(name = "service_code", length = 50, nullable = false)
    @Builder.Default
    private String serviceCode = "LEDGER_SERVICE";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private RecordStatus status = RecordStatus.ACTIVE;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "is_allow", nullable = false)
    @Builder.Default
    private Boolean isAllow = Boolean.FALSE;
}

