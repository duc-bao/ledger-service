package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.ActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ActionLogRepository extends JpaRepository<ActionLog, String>, JpaSpecificationExecutor<ActionLog> {
}
