package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.Menu;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, String>, JpaSpecificationExecutor<Menu> {
    Optional<Menu> findByCode(String code);

    Optional<Menu> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByParentId(String parentId);

    Optional<Menu> findByIdAndStatus(String id, RecordStatus status);

    List<Menu> findAllByOrderByOffsetAscCodeAsc();

    List<Menu> findByStatusAndVisibleTrueOrderByOffsetAscCodeAsc(RecordStatus status);

    @Query(value = """
            SELECT *
            FROM sys_menus m
            WHERE POSITION(
                CONCAT(',', :ancestorId, ',')
                IN CONCAT(',', COALESCE(m.ancestors, ''), ',')
            ) > 0
            """, nativeQuery = true)
    List<Menu> findByAncestorId(@Param("ancestorId") String ancestorId);
    @Query("SELECT m FROM Menu m WHERE UPPER(m.code) IN :codes ORDER BY m.offset ASC, m.code ASC")
    List<Menu> findByCodeInOrderByOffset(@Param("codes") Collection<String> codes);
}
