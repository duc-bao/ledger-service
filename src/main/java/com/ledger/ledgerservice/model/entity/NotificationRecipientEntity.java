package com.ledger.ledgerservice.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ledger.ledgerservice.model.enums.NotificationReadStatus;
import com.ledger.ledgerservice.util.NotificationReadStatusConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "sys_notification_recipients",
        indexes = {
                @Index(columnList = "recipient_user_id, created_at"),
                @Index(columnList = "recipient_user_id, read_status, created_at"),
                @Index(columnList = "notification_id")
        })
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class NotificationRecipientEntity {

    @jakarta.persistence.Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "notification_id", nullable = false, length = 36)
    private String notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", referencedColumnName = "id", insertable = false, updatable = false)
    private NotificationEntity notification;

    @Column(name = "recipient_user_id", nullable = false, length = 36)
    private String recipientUserId;

    @Column(name = "read_status", nullable = false, length = 20)
    @Convert(converter = NotificationReadStatusConverter.class)
    @Builder.Default
    private NotificationReadStatus readStatus = NotificationReadStatus.UNREAD;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "is_pinned", nullable = false)
    @Builder.Default
    private Boolean isPinned = Boolean.FALSE;

    @Column(name = "dismissed_at")
    private LocalDateTime dismissedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
