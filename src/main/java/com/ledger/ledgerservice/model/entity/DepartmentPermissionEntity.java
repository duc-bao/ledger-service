package com.ledger.ledgerservice.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ledger.ledgerservice.util.StringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "org_department_permissions",
        indexes = {
                @Index(columnList = "department_id"),
                @Index(columnList = "menu_id"),
                @Index(columnList = "discriminator")
        })
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DepartmentPermissionEntity extends EntityBase {

    @Column(name = "department_id", nullable = false)
    private String departmentId;

    @Column(name = "menu_id", nullable = false)
    private String menuId;

    @Column(name = "discriminator", length = 100, nullable = false)
    @Builder.Default
    private String discriminator = "DEPARTMENT";

    @Column(name = "actions", length = 700)
    @Convert(converter = StringListConverter.class)
    @Builder.Default
    private List<String> actions = new ArrayList<>();

    @Column(name = "inherited", nullable = false)
    @Builder.Default
    private Boolean inherited = Boolean.TRUE;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;
}
