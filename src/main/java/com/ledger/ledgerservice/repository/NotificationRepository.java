package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotificationRepository extends JpaRepository<NotificationEntity, String>,
        JpaSpecificationExecutor<NotificationEntity> {
}
