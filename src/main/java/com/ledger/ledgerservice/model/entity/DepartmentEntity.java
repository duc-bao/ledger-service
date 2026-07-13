package com.ledger.ledgerservice.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "org_departments",
        indexes = {
                @Index(columnList = "parent_id"),
                @Index(columnList = "code"),
                @Index(columnList = "name"),
                @Index(columnList = "ancestors"),
                @Index(columnList = "manager_user_id")
        })
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DepartmentEntity extends EntityBase {

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "short_name", length = 100)
    private String shortName;

    @Column(name = "parent_id")
    private String parentId;

    @Column(name = "ancestors", length = 1000)
    private String ancestors;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "tree_level", nullable = false)
    @Builder.Default
    private Integer treeLevel = 0;

    @Column(name = "manager_user_id")
    private String managerUserId;

    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = Boolean.TRUE;
}
