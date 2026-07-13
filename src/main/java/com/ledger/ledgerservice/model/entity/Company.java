package com.ledger.ledgerservice.model.entity;

import com.ledger.ledgerservice.model.enums.CompanyStatus;
import com.ledger.ledgerservice.model.enums.CompanyType;
import com.ledger.ledgerservice.model.enums.UnitLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "companies",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"parent_id", "sort_level"}, name = "parent_id_sort_level_unique")})
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Company extends EntityBase {
    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "code", length = 50, unique = true)
    private String code;

    @Column(name = "short_name", length = 50)
    private String shortName;

    @Column(name = "unit_level")
    @Enumerated(EnumType.STRING)
    private UnitLevel unitLevel;

    @Column(name = "parent_id")
    private String parentId;

    @Column(name = "company_type")
    @Enumerated(EnumType.STRING)
    private CompanyType companyType;

    @Column(name = "sort_level")
    private Integer sortLevel;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CompanyStatus status;
}
