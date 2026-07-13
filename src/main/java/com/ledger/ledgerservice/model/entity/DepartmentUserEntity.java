package com.ledger.ledgerservice.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "org_department_users",
        indexes = {
                @Index(columnList = "department_id"),
                @Index(columnList = "user_id"),
                @Index(columnList = "is_primary")
        })
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DepartmentUserEntity extends EntityBase {

    @Column(name = "department_id", nullable = false)
    private String departmentId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = Boolean.TRUE;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "join_date")
    private LocalDateTime joinDate;

    @Column(name = "left_date")
    private LocalDateTime leftDate;
}
