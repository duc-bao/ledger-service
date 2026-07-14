package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.UserGroup;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserGroupRepository extends JpaRepository<UserGroup, String> {
    List<UserGroup> findByUserId(String userId);

    List<UserGroup> findByUserIdAndStatus(String userId, RecordStatus status);

    List<UserGroup> findByGroupId(String groupId);

    List<UserGroup> findByGroupIdAndStatus(String groupId, RecordStatus status);

    List<UserGroup> findByUserIdIn(Collection<String> userIds);

    List<UserGroup> findByUserIdInAndStatus(Collection<String> userIds, RecordStatus status);

    Optional<UserGroup> findByUserIdAndGroupId(String userId, String groupId);

    @Query("""
            SELECT ug FROM UserGroup ug
            WHERE ug.userId = :userId
              AND ug.status = :status
              AND (ug.effectiveFrom IS NULL OR ug.effectiveFrom <= :at)
              AND (ug.effectiveTo IS NULL OR ug.effectiveTo >= :at)
            ORDER BY ug.id DESC
            """)
    List<UserGroup> findActiveByUserIdAt(@Param("userId") String userId,
                                         @Param("status") RecordStatus status,
                                         @Param("at") LocalDateTime at);

    @Query("""
            SELECT ug FROM UserGroup ug
            WHERE ug.userId IN :userIds
              AND ug.status = :status
              AND (ug.effectiveFrom IS NULL OR ug.effectiveFrom <= :at)
              AND (ug.effectiveTo IS NULL OR ug.effectiveTo >= :at)
            ORDER BY ug.id DESC
            """)
    List<UserGroup> findActiveByUserIdInAt(@Param("userIds") Collection<String> userIds,
                                           @Param("status") RecordStatus status,
                                           @Param("at") LocalDateTime at);

    void deleteByUserId(String userId);
}

