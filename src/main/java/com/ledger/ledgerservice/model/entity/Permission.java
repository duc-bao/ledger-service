package com.ledger.ledgerservice.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "idp_permission_definitions", indexes = {
        @Index(name = "idx_idp_permission_definitions_module_code", columnList = "module_code"),
        @Index(name = "idx_idp_permission_definitions_status", columnList = "status")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Permission extends EntityBase {

    @Column(name = "code", length = 100, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "module_code", length = 50)
    private String moduleCode;

    @Column(name = "action_code", length = 50)
    private String actionCode;

    @Column(name = "resource_type", length = 50)
    private String resourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private RecordStatus status = RecordStatus.ACTIVE;
}