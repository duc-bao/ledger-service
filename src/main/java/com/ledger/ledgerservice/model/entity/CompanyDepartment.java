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
@Table(name = "org_company_departments", indexes = {
        @Index(columnList = "company_id"),
        @Index(columnList = "department_id"),
        @Index(columnList = "status")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class CompanyDepartment extends EntityBase {
    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Column(name = "department_id", nullable = false)
    private String departmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RecordStatus status = RecordStatus.ACTIVE;
}
