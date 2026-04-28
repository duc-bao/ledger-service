package com.ledger.ledgerservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "idp_users", indexes = {
        @Index(columnList = "user_name"),
        @Index(columnList = "email"),
        @Index(columnList = "full_name"),
        @Index(columnList = "phone"),
        @Index(columnList = "status")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class User extends EntityBase {
    @Column(name = "user_name", length = 50, unique = true)
    @Size(max = 50, message = "Max length is 50 characters")
    private String username;

    @Column(name = "password")
    @JsonIgnore
    @Builder.Default
    private String password = null;

    @Column(name = "email", length = 50, unique = true)
    @Size(max = 50, message = "Max length is 50 characters")
    private String email;

    @Column(name = "phone", length = 15, unique = true)
    private String phone;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "user_type")
    private String userType;

    @Column(name = "require_change", nullable = false)
    @Builder.Default
    private Boolean requireChange = false;
}
