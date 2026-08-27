package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.DepartmentEntity;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<DepartmentEntity, String> {
    Optional<DepartmentEntity> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<DepartmentEntity> findByParentIdOrderBySortOrderAscCodeAsc(String parentId);

    List<DepartmentEntity> findByIsActiveTrueOrderByTreeLevelAscSortOrderAscCodeAsc();

    @QueryHints(value = {
        @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE, value = "500"),
        @QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_CACHEABLE, value = "false")
    })
    @Query("""
            SELECT new com.ledger.ledgerservice.model.dto.excel.DepartmentExportRow(
                d.code, d.name, d.shortName, d.parentId, d.status, d.isActive, d.createdAt
            )
            FROM DepartmentEntity d
            WHERE d.createdAt >= :startDate
              AND d.createdAt <= :endDate
            ORDER BY d.createdAt DESC
            """)
    java.util.stream.Stream<com.ledger.ledgerservice.model.dto.excel.DepartmentExportRow> streamForExport(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
