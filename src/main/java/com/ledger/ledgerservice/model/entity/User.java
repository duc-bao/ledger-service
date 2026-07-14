package com.ledger.ledgerservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ledger.ledgerservice.model.enums.UserStatus;
import lombok.Builder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "idp_users", indexes = {
        @Index(columnList = "user_name"),
        @Index(columnList = "email"),
        @Index(columnList = "full_name"),
        @Index(columnList = "phone"),
        @Index(columnList = "status"),
        @Index(columnList = "locked_until")
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

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
}

