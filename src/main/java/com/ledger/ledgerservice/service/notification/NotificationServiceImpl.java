package com.ledger.ledgerservice.service.notification;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.request.CreateNotificationRequest;
import com.ledger.ledgerservice.model.dto.request.UpdateNotificationRequest;
import com.ledger.ledgerservice.model.dto.response.MetaDataResp;
import com.ledger.ledgerservice.model.dto.response.NotificationListResponse;
import com.ledger.ledgerservice.model.dto.response.NotificationResponse;
import com.ledger.ledgerservice.model.entity.NotificationEntity;
import com.ledger.ledgerservice.model.entity.NotificationRecipientEntity;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.NotificationReadStatus;
import com.ledger.ledgerservice.model.enums.UserStatus;
import com.ledger.ledgerservice.model.mapper.NotificationListResponseData;
import com.ledger.ledgerservice.model.mapper.NotificationMapStructMapper;
import com.ledger.ledgerservice.model.security.JwtUserPrincipal;
import com.ledger.ledgerservice.repository.NotificationRecipientRepository;
import com.ledger.ledgerservice.repository.NotificationRepository;
import com.ledger.ledgerservice.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository notificationRecipientRepository;
    private final UserRepository userRepository;
    private final NotificationMapStructMapper notificationMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMyNotifications(Integer page, Integer size) {
        String currentUserId = getCurrentUserId();

        Page<NotificationRecipientEntity> resultPage = notificationRecipientRepository.findMyNotifications(
                currentUserId,
                PageRequest.of(normalizePage(page), normalizeSize(size))
        );

        List<NotificationResponse> items = resultPage.map(notificationMapper::toResponse).getContent();

        long unreadCount = notificationRecipientRepository.countByRecipientUserIdAndDeletedAtIsNullAndReadStatus(
                currentUserId,
                NotificationReadStatus.UNREAD
        );

        MetaDataResp metaData = MetaDataResp.builder()
                .page(resultPage.getNumber() + 1)
                .size(resultPage.getSize())
                .totalElements(resultPage.getTotalElements())
                .totalPages(resultPage.getTotalPages())
                .build();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("items", items);
        responseData.put("unreadCount", unreadCount);
        responseData.put("pagination", metaData);

        return responseData;
    }

    @Override
    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        String currentUserId = getCurrentUserId();
        String currentUsername = getCurrentUsername();
        User sender = findCurrentUser(currentUserId, currentUsername);

        NotificationEntity notification = notificationMapper.toEntity(
                request,
                currentUserId,
                resolveSenderName(sender, currentUsername),
                currentUsername
        );

        NotificationEntity saved = notificationRepository.save(notification);

        Set<String> recipientUserIds = resolveRecipientUserIds(request.getRecipientUserIds());
        if (recipientUserIds.isEmpty()) {
            throw new BusinessException(MessageCode.NOTIFICATION_RECIPIENT_NOT_FOUND, org.springframework.http.HttpStatus.NOT_FOUND);
        }

        List<NotificationRecipientEntity> recipients = recipientUserIds.stream()
                .map(recipientUserId -> notificationMapper.toRecipientEntity(saved.getId(), recipientUserId))
                .toList();
        notificationRecipientRepository.saveAll(recipients);

        return notificationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public NotificationResponse updateNotification(String notificationId, UpdateNotificationRequest request) {
        String currentUserId = getCurrentUserId();
        String currentUsername = getCurrentUsername();

        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(MessageCode.NOTIFICATION_NOT_FOUND, org.springframework.http.HttpStatus.NOT_FOUND));

        if (!currentUserId.equals(notification.getSenderUserId())) {
            throw new BusinessException(MessageCode.FORBIDDEN, org.springframework.http.HttpStatus.FORBIDDEN);
        }

        notificationMapper.updateEntity(request, notification, currentUsername);
        NotificationEntity saved = notificationRepository.save(notification);

        syncRecipients(saved.getId(), request.getRecipientUserIds());

        entityManager.flush();

        return notificationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(String notificationId) {
        String currentUserId = getCurrentUserId();
        int updated = notificationRecipientRepository.markAsRead(
                notificationId,
                currentUserId,
                NotificationReadStatus.READ,
                LocalDateTime.now()
        );
        if (updated == 0) {
            throw new BusinessException(MessageCode.NOTIFICATION_NOT_FOUND, org.springframework.http.HttpStatus.NOT_FOUND);
        }
        return notificationMapper.toResponse(notificationRecipientRepository.findByNotificationIdAndRecipientUserIdAndDeletedAtIsNull(notificationId, currentUserId)
                .orElseThrow(() -> new BusinessException(MessageCode.NOTIFICATION_NOT_FOUND, org.springframework.http.HttpStatus.NOT_FOUND)));
    }

    @Override
    @Transactional
    public void deleteNotification(String notificationId) {
        String currentUserId = getCurrentUserId();
        int updated = notificationRecipientRepository.softDelete(notificationId, currentUserId, LocalDateTime.now());
        if (updated == 0) {
            throw new BusinessException(MessageCode.NOTIFICATION_NOT_FOUND, org.springframework.http.HttpStatus.NOT_FOUND);
        }
    }

    private void syncRecipients(String notificationId, List<String> requestedRecipientIds) {
        if (requestedRecipientIds == null) {
            return;
        }

        Set<String> normalized = resolveRecipientUserIds(requestedRecipientIds);
        if (normalized.isEmpty()) {
            throw new BusinessException(MessageCode.NOTIFICATION_RECIPIENT_NOT_FOUND, org.springframework.http.HttpStatus.NOT_FOUND);
        }

        List<NotificationRecipientEntity> existingRecipients = notificationRecipientRepository.findByNotificationId(notificationId);
        Map<String, NotificationRecipientEntity> existingByUserId = existingRecipients.stream()
                .collect(Collectors.toMap(NotificationRecipientEntity::getRecipientUserId, r -> r, (left, right) -> left, LinkedHashMap::new));

        for (String recipientUserId : normalized) {
            NotificationRecipientEntity recipient = existingByUserId.remove(recipientUserId);
            if (recipient == null) {
                notificationRecipientRepository.save(notificationMapper.toRecipientEntity(notificationId, recipientUserId));
                continue;
            }

            if (recipient.getDeletedAt() != null) {
                recipient.setDeletedAt(null);
                recipient.setReadStatus(NotificationReadStatus.UNREAD);
                recipient.setReadAt(null);
                recipient.setCreatedAt(LocalDateTime.now());
            }
            notificationRecipientRepository.save(recipient);
        }

        existingByUserId.values().forEach(recipient -> {
            if (recipient.getDeletedAt() == null) {
                recipient.setDeletedAt(LocalDateTime.now());
                notificationRecipientRepository.save(recipient);
            }
        });
    }

    private Set<String> resolveRecipientUserIds(List<String> recipientUserIds) {
        Set<String> normalized = normalizeRecipientUserIds(recipientUserIds);
        if (!normalized.isEmpty()) {
            return normalized;
        }
        return findActiveRecipientUserIds();
    }

    private Set<String> normalizeRecipientUserIds(List<String> recipientUserIds) {
        if (CollectionUtils.isEmpty(recipientUserIds)) {
            return Set.of();
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String recipientUserId : recipientUserIds) {
            if (StringUtils.hasText(recipientUserId)) {
                normalized.add(recipientUserId.trim());
            }
        }
        return normalized;
    }

    private Set<String> findActiveRecipientUserIds() {
        return userRepository.findAllByStatus(UserStatus.ACTIVE).stream()
                .map(User::getId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private User findCurrentUser(String currentUserId, String currentUsername) {
        return userRepository.findById(currentUserId)
                .orElseGet(() -> userRepository.findByUsername(currentUsername)
                        .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND, org.springframework.http.HttpStatus.UNAUTHORIZED)));
    }

    private String resolveSenderName(User sender, String fallbackUsername) {
        if (sender != null && StringUtils.hasText(sender.getFullName())) {
            return sender.getFullName();
        }
        if (sender != null && StringUtils.hasText(sender.getUsername())) {
            return sender.getUsername();
        }
        return fallbackUsername;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException(MessageCode.UNAUTHORIZED, org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
            return jwtUserPrincipal.userId();
        }
        throw new BusinessException(MessageCode.UNAUTHORIZED, org.springframework.http.HttpStatus.UNAUTHORIZED);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtUserPrincipal jwtUserPrincipal) {
            return jwtUserPrincipal.username();
        }
        return null;
    }

    private int normalizePage(Integer page) {
        int requestPage = page == null ? 1 : page;
        return requestPage <= 0 ? 0 : requestPage - 1;
    }

    private int normalizeSize(Integer size) {
        int requestSize = size == null ? 20 : size;
        return Math.min(Math.max(requestSize, 1), 200);
    }
}
