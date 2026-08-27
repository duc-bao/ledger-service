package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.ActionLog;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ActionLogRepository extends JpaRepository<ActionLog, String>, JpaSpecificationExecutor<ActionLog> {

    @QueryHints(value = {
        @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE, value = "500"),
        @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_CACHEABLE, value = "false")
    })
    @Query("""
            SELECT new com.ledger.ledgerservice.model.dto.excel.AuditLogExportRow(
                a.requestId, a.username, a.service, a.action, a.requestMethod, a.requestUrlPath, a.statusCode, a.durationMs, a.createdAt
            )
            FROM ActionLog a
            WHERE a.createdAt >= :startDate
              AND a.createdAt <= :endDate
            ORDER BY a.createdAt DESC
            """)
    java.util.stream.Stream<com.ledger.ledgerservice.model.dto.excel.AuditLogExportRow> streamForExport(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
