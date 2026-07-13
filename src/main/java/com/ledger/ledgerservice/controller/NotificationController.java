package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.CreateNotificationRequest;
import com.ledger.ledgerservice.model.dto.request.UpdateNotificationRequest;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.MetaDataResp;
import com.ledger.ledgerservice.model.dto.response.NotificationListResponse;
import com.ledger.ledgerservice.model.dto.response.NotificationResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.notification.NotificationService;
import com.ledger.ledgerservice.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "APIs for notification inbox and management")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;
    private final ResponseHelper responseHelper;

    @GetMapping("/me")
    @Operation(summary = "Get my notifications")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getMyNotifications(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        Map<String, Object> notificationPage = notificationService.getMyNotifications(page, size);

        MetaDataResp metaData = (MetaDataResp) notificationPage.get("pagination");

        Map<String, Object> apiData = new LinkedHashMap<>();
        apiData.put("items", notificationPage.get("items"));
        apiData.put("unreadCount", notificationPage.get("unreadCount"));

        // 4. Đẩy qua ResponseHelper. Tham số generic lúc này khớp hoàn toàn là Map<String, Object>
        return responseHelper.ok(
                MessageCode.SUCCESS,
                apiData,
                metaData,
                HttpStatus.OK
        );
    }

    @PostMapping
    @Operation(summary = "Create notification")
    public ResponseEntity<BaseResponse<NotificationResponse>> createNotification
            (@Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return responseHelper.ok(MessageCode.NOTIFICATION_CREATED, response, HttpStatus.CREATED);
    }

    @PutMapping("/{notificationId}")
    @Operation(summary = "Update notification")
    public ResponseEntity<BaseResponse<NotificationResponse>> updateNotification(
            @PathVariable String notificationId,
            @Valid @RequestBody UpdateNotificationRequest request
    ) {
        NotificationResponse response = notificationService.updateNotification(notificationId, request);
        return responseHelper.ok(MessageCode.NOTIFICATION_UPDATED, response, HttpStatus.OK);
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<BaseResponse<NotificationResponse>> markAsRead(@PathVariable String notificationId) {
        NotificationResponse response = notificationService.markAsRead(notificationId);
        return responseHelper.ok(MessageCode.NOTIFICATION_MARKED_AS_READ, response, HttpStatus.OK);
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete notification for current user")
    public ResponseEntity<BaseResponse<Object>> deleteNotification(@PathVariable String notificationId) {
        notificationService.deleteNotification(notificationId);
        return responseHelper.ok(MessageCode.NOTIFICATION_DELETED, HttpStatus.OK);
    }
}
