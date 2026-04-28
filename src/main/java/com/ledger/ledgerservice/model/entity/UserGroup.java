package com.ledger.ledgerservice.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "idp_user_groups",
        indexes = {@Index(columnList = "group_id"), @Index(columnList = "user_id")}
)
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
    @Column(name = "group_id")
    private String groupId;

    @NotEmpty(message = "Id of user can not empty")
    @NotNull(message = "Id of user can not empty")
    @Column(name = "user_id")
    private String userId;
}
