package com.ledger.ledgerservice.model.mapper;

import com.ledger.ledgerservice.model.dto.request.CreateNotificationRequest;
import com.ledger.ledgerservice.model.dto.request.UpdateNotificationRequest;
import com.ledger.ledgerservice.model.dto.response.NotificationListResponse;
import com.ledger.ledgerservice.model.dto.response.NotificationResponse;
import com.ledger.ledgerservice.model.entity.NotificationEntity;
import com.ledger.ledgerservice.model.entity.NotificationRecipientEntity;
import com.ledger.ledgerservice.model.enums.NotificationReadStatus;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Condition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapStructMapper {

    @Mapping(target = "notificationId", source = "id")
    NotificationResponse toResponse(NotificationEntity notification);

    @Mapping(target = "notificationId", source = "notificationId")
    @Mapping(target = "recipientId", source = "id")
    @Mapping(target = "senderUserId", source = "notification.senderUserId")
    @Mapping(target = "senderName", source = "notification.senderName")
    @Mapping(target = "title", source = "notification.title")
    @Mapping(target = "content", source = "notification.content")
    @Mapping(target = "type", source = "notification.type")
    @Mapping(target = "status", source = "notification.status")
    @Mapping(target = "priority", source = "notification.priority")
    @Mapping(target = "actionLabel", source = "notification.actionLabel")
    @Mapping(target = "targetUrl", source = "notification.targetUrl")
    @Mapping(target = "fileUrl", source = "notification.fileUrl")
    @Mapping(target = "fileName", source = "notification.fileName")
    @Mapping(target = "icon", source = "notification.icon")
    @Mapping(target = "relatedEntityType", source = "notification.relatedEntityType")
    @Mapping(target = "relatedEntityId", source = "notification.relatedEntityId")
    @Mapping(target = "isDismissible", source = "notification.isDismissible")
    @Mapping(target = "expiresAt", source = "notification.expiresAt")
    @Mapping(target = "metadataJson", source = "notification.metadataJson")
    @Mapping(target = "createdAt", source = "notification.createdAt")
    @Mapping(target = "updatedAt", source = "notification.updatedAt")
    @Mapping(target = "isPinned", source = "notification.isPinned")
    @Mapping(target = "recipientUserId", source = "recipientUserId")
    @Mapping(target = "readStatus", source = "readStatus")
    @Mapping(target = "readAt", source = "readAt")
    @Mapping(target = "dismissedAt", source = "dismissedAt")
    @Mapping(target = "deletedAt", source = "deletedAt")
    @Mapping(target = "isRead", expression = "java(isRead(recipient.getReadStatus()))")
    NotificationResponse toResponse(NotificationRecipientEntity recipient);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "senderUserId", source = "currentUserId")
    @Mapping(target = "senderName", source = "senderName")
    @Mapping(target = "title", source = "request.title", qualifiedByName = "trimToNull")
    @Mapping(target = "content", source = "request.content", qualifiedByName = "trimToNull")
    @Mapping(target = "type", source = "request.type", qualifiedByName = "trimToNull")
    @Mapping(target = "status", source = "request.status", conditionQualifiedByName = "hasText", qualifiedByName = "trimToNull")
    @Mapping(target = "priority", source = "request.priority", conditionQualifiedByName = "hasText", qualifiedByName = "trimToNull")
    @Mapping(target = "actionLabel", source = "request.actionLabel", qualifiedByName = "trimToNull")
    @Mapping(target = "targetUrl", source = "request.targetUrl", qualifiedByName = "trimToNull")
    @Mapping(target = "fileUrl", source = "request.fileUrl", qualifiedByName = "trimToNull")
    @Mapping(target = "fileName", source = "request.fileName", qualifiedByName = "trimToNull")
    @Mapping(target = "icon", source = "request.icon", qualifiedByName = "trimToNull")
    @Mapping(target = "relatedEntityType", source = "request.relatedEntityType", qualifiedByName = "trimToNull")
    @Mapping(target = "relatedEntityId", source = "request.relatedEntityId", qualifiedByName = "trimToNull")
    @Mapping(target = "isDismissible", source = "request.isDismissible")
    @Mapping(target = "expiresAt", source = "request.expiresAt")
    @Mapping(target = "metadataJson", source = "request.metadataJson")
    NotificationEntity toEntity(CreateNotificationRequest request, String currentUserId, String senderName, String currentUsername);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "updatedBy", source = "currentUsername")
    @Mapping(target = "title", source = "request.title", conditionQualifiedByName = "hasText", qualifiedByName = "trimToNull")
    @Mapping(target = "content", source = "request.content", conditionQualifiedByName = "hasText", qualifiedByName = "trimToNull")
    @Mapping(target = "type", source = "request.type", conditionQualifiedByName = "hasText", qualifiedByName = "trimToNull")
    @Mapping(target = "status", source = "request.status", conditionQualifiedByName = "hasText", qualifiedByName = "trimToNull")
    @Mapping(target = "priority", source = "request.priority", conditionQualifiedByName = "hasText", qualifiedByName = "trimToNull")
    @Mapping(target = "actionLabel", source = "request.actionLabel", qualifiedByName = "trimToNull")
    @Mapping(target = "targetUrl", source = "request.targetUrl", qualifiedByName = "trimToNull")
    @Mapping(target = "fileUrl", source = "request.fileUrl", qualifiedByName = "trimToNull")
    @Mapping(target = "fileName", source = "request.fileName", qualifiedByName = "trimToNull")
    @Mapping(target = "icon", source = "request.icon", qualifiedByName = "trimToNull")
    @Mapping(target = "relatedEntityType", source = "request.relatedEntityType", qualifiedByName = "trimToNull")
    @Mapping(target = "relatedEntityId", source = "request.relatedEntityId", qualifiedByName = "trimToNull")
    @Mapping(target = "isDismissible", source = "request.isDismissible")
    @Mapping(target = "expiresAt", source = "request.expiresAt")
    @Mapping(target = "metadataJson", source = "request.metadataJson")
    void updateEntity(UpdateNotificationRequest request, @MappingTarget NotificationEntity notification, String currentUsername);

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "notification", ignore = true)
    @Mapping(target = "readStatus", expression = "java(com.ledger.ledgerservice.model.enums.NotificationReadStatus.UNREAD)")
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "isPinned", ignore = true)
    @Mapping(target = "dismissedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    NotificationRecipientEntity toRecipientEntity(String notificationId, String recipientUserId);

    NotificationListResponse toListResponse(NotificationListResponseData data);

    @AfterMapping
    default void applyDefaults(@MappingTarget NotificationEntity notification) {
        if (!StringUtils.hasText(notification.getStatus())) {
            notification.setStatus("ACTIVE");
        }
        if (!StringUtils.hasText(notification.getPriority())) {
            notification.setPriority("NORMAL");
        }
        if (notification.getIsPinned() == null) {
            notification.setIsPinned(Boolean.FALSE);
        }
        if (notification.getIsDismissible() == null) {
            notification.setIsDismissible(Boolean.TRUE);
        }
        if (notification.getMetadataJson() == null) {
            notification.setMetadataJson(new LinkedHashMap<>());
        }
    }

    @AfterMapping
    default void applyRecipientDefaults(@MappingTarget NotificationRecipientEntity recipient) {
        if (recipient.getReadStatus() == null) {
            recipient.setReadStatus(NotificationReadStatus.UNREAD);
        }
        if (recipient.getIsPinned() == null) {
            recipient.setIsPinned(Boolean.FALSE);
        }
        if (recipient.getCreatedAt() == null) {
            recipient.setCreatedAt(LocalDateTime.now());
        }
    }

    @Named("trimToNull")
    default String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    @Condition
    @Named("hasText")
    default boolean hasText(String value) {
        return StringUtils.hasText(value);
    }

    default boolean isRead(NotificationReadStatus readStatus) {
        return readStatus == NotificationReadStatus.READ;
    }
}
