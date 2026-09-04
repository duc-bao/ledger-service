package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.DepartmentEntity;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("""
            SELECT d FROM DepartmentEntity d
            WHERE (:status IS NULL OR :status = '' OR UPPER(d.status) = UPPER(:status))
              AND (:isActive IS NULL OR d.isActive = :isActive)
              AND (
                  :keyword IS NULL OR :keyword = ''
                  OR LOWER(d.code) LIKE CONCAT('%', :keyword, '%')
                  OR LOWER(d.name) LIKE CONCAT('%', :keyword, '%')
                  OR LOWER(d.shortName) LIKE CONCAT('%', :keyword, '%')
              )
            """)
    Page<DepartmentEntity> searchDepartments(@Param("keyword") String keyword,
                                             @Param("status") String status,
                                             @Param("isActive") Boolean isActive,
                                             Pageable pageable);

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
