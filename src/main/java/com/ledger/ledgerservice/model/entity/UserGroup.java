package com.ledger.ledgerservice.model.entity;

import com.ledger.ledgerservice.model.enums.RecordStatus;
import lombok.Builder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Table(name = "idp_user_groups", indexes = {
        @Index(columnList = "group_id"),
        @Index(columnList = "user_id"),
        @Index(columnList = "status")
})
@Entity
@Getter
@Setter
@Builder
public class UserGroup {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotEmpty(message = "Id of group can not empty")
    @NotNull(message = "Id of group can not empty")
    @Column(name = "group_id", nullable = false)
    private String groupId;

    @NotEmpty(message = "Id of user can not empty")
    @NotNull(message = "Id of user can not empty")
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RecordStatus status = RecordStatus.ACTIVE;
}

