package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class NotificationResponse {
    private String notificationId;
    private String recipientId;
    private String recipientUserId;
    private String senderUserId;
    private String senderName;
    private String title;
    private String content;
    private String type;
    private String status;
    private String priority;
    private String actionLabel;
    private String targetUrl;
    private String fileUrl;
    private String fileName;
    private String icon;
    private String relatedEntityType;
    private String relatedEntityId;
    private Boolean isDismissible;
    private LocalDateTime expiresAt;
    private Map<String, Object> metadataJson;
    private String readStatus;
    private LocalDateTime readAt;
    private Boolean isPinned;
    private LocalDateTime dismissedAt;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isRead;
}
