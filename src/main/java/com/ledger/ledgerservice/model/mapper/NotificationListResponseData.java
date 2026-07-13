package com.ledger.ledgerservice.model.mapper;

import com.ledger.ledgerservice.model.dto.response.NotificationResponse;

import java.util.List;

public record NotificationListResponseData(List<NotificationResponse> items, long unreadCount) {
}
