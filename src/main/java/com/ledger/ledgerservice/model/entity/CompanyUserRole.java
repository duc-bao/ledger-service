package com.ledger.ledgerservice.model.entity;

import com.ledger.ledgerservice.model.enums.RecordStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "org_company_user_roles", indexes = {
        @Index(columnList = "company_id"),
        @Index(columnList = "user_id"),
        @Index(columnList = "group_id"),
        @Index(columnList = "status")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CompanyUserRole extends EntityBase {
    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "group_id", nullable = false)
    private String groupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RecordStatus status = RecordStatus.ACTIVE;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;
}
