package com.ledger.ledgerservice.service.notification;

import com.ledger.ledgerservice.model.dto.request.CreateNotificationRequest;
import com.ledger.ledgerservice.model.dto.request.UpdateNotificationRequest;
import com.ledger.ledgerservice.model.dto.response.NotificationListResponse;
import com.ledger.ledgerservice.model.dto.response.NotificationResponse;

import java.util.Map;

public interface NotificationService {
    Map<String, Object> getMyNotifications(Integer page, Integer size);

    NotificationResponse createNotification(CreateNotificationRequest request);

    NotificationResponse updateNotification(String notificationId, UpdateNotificationRequest request);

    NotificationResponse markAsRead(String notificationId);

    void deleteNotification(String notificationId);
}
