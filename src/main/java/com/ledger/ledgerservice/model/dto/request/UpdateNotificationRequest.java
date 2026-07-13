package com.ledger.ledgerservice.model.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class UpdateNotificationRequest {

    private List<@Size(max = 36) String> recipientUserIds;

    @Size(max = 255)
    private String title;

    private String content;

    @Size(max = 50)
    private String type;

    @Size(max = 30)
    private String status;

    @Size(max = 20)
    private String priority;

    @Size(max = 100)
    private String actionLabel;

    @Size(max = 1000)
    private String targetUrl;

    @Size(max = 1000)
    private String fileUrl;

    @Size(max = 255)
    private String fileName;

    @Size(max = 100)
    private String icon;

    @Size(max = 100)
    private String relatedEntityType;

    @Size(max = 36)
    private String relatedEntityId;

    private Boolean isDismissible;

    private LocalDateTime expiresAt;

    private Map<String, Object> metadataJson;
}
