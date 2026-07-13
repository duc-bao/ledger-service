package com.ledger.ledgerservice.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "sys_notifications")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class NotificationEntity extends EntityBase {

    @Column(name = "sender_user_id", length = 36)
    private String senderUserId;

    @Column(name = "sender_name", length = 100)
    private String senderName;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private String priority = "NORMAL";

    @Column(name = "action_label", length = 100)
    private String actionLabel;

    @Column(name = "target_url", length = 1000)
    private String targetUrl;

    @Column(name = "file_url", length = 1000)
    private String fileUrl;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "related_entity_type", length = 100)
    private String relatedEntityType;

    @Column(name = "related_entity_id", length = 36)
    private String relatedEntityId;

    @Column(name = "is_pinned", nullable = false)
    @Builder.Default
    private Boolean isPinned = Boolean.FALSE;

    @Column(name = "is_dismissible", nullable = false)
    @Builder.Default
    private Boolean isDismissible = Boolean.TRUE;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_json", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadataJson = new LinkedHashMap<>();
}
