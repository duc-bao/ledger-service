package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.Group;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, String> {
    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, String id);

    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);

    Optional<Group> findByCodeIgnoreCase(String code);

    Optional<Group> findByCodeIgnoreCaseAndStatus(String code, RecordStatus status);

    Optional<Group> findByIdAndStatus(String id, RecordStatus status);

    boolean existsByIdAndStatus(String id, RecordStatus status);

    List<Group> findByStatusOrderBySortOrderAscCodeAsc(RecordStatus status);

    @Query("""
            SELECT g FROM Group g
            WHERE (:status IS NULL OR g.status = :status)
              AND (
                  :keyword IS NULL OR :keyword = ''
                  OR LOWER(g.code) LIKE CONCAT('%', :keyword, '%')
                  OR LOWER(g.name) LIKE CONCAT('%', :keyword, '%')
                  OR LOWER(g.description) LIKE CONCAT('%', :keyword, '%')
              )
            """)
    Page<Group> searchRoles(@Param("keyword") String keyword,
                            @Param("status") RecordStatus status,
                            Pageable pageable);
}
