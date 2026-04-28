package com.ledger.ledgerservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "idp_groups"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Group extends EntityBase {
    @Column(name = "code", length = 50, unique = true)
    private String code;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @JsonIgnore
    @Column(name = "is_super_admin", updatable = false)
    @Builder.Default
    private Boolean isSuperAdmin = false;
}
