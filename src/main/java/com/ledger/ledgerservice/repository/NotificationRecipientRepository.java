package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.NotificationRecipientEntity;
import com.ledger.ledgerservice.model.enums.NotificationReadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipientEntity, String>,
        JpaSpecificationExecutor<NotificationRecipientEntity> {

    @Query(value = """
            select nr
            from NotificationRecipientEntity nr
            join fetch nr.notification n
            where nr.recipientUserId = :recipientUserId
              and nr.deletedAt is null
            order by nr.createdAt desc
            """,
            countQuery = """
                    select count(nr)
                    from NotificationRecipientEntity nr
                    where nr.recipientUserId = :recipientUserId
                      and nr.deletedAt is null
                    """)
    Page<NotificationRecipientEntity> findMyNotifications(@Param("recipientUserId") String recipientUserId, Pageable pageable);

    long countByRecipientUserIdAndDeletedAtIsNullAndReadStatus(String recipientUserId, NotificationReadStatus readStatus);

    Optional<NotificationRecipientEntity> findByNotificationIdAndRecipientUserIdAndDeletedAtIsNull(String notificationId, String recipientUserId);

    List<NotificationRecipientEntity> findByNotificationId(String notificationId);

    List<NotificationRecipientEntity> findByNotificationIdAndDeletedAtIsNull(String notificationId);

    @Modifying
    @Query("""
            update NotificationRecipientEntity nr
               set nr.readStatus = :readStatus,
                   nr.readAt = :readAt
             where nr.notificationId = :notificationId
               and nr.recipientUserId = :recipientUserId
               and nr.deletedAt is null
            """)
    int markAsRead(@Param("notificationId") String notificationId,
                   @Param("recipientUserId") String recipientUserId,
                   @Param("readStatus") NotificationReadStatus readStatus,
                   @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("""
            update NotificationRecipientEntity nr
               set nr.deletedAt = :deletedAt
             where nr.notificationId = :notificationId
               and nr.recipientUserId = :recipientUserId
               and nr.deletedAt is null
            """)
    int softDelete(@Param("notificationId") String notificationId,
                   @Param("recipientUserId") String recipientUserId,
                   @Param("deletedAt") LocalDateTime deletedAt);
}
