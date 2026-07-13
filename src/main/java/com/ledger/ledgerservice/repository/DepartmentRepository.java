package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<DepartmentEntity, String> {
    Optional<DepartmentEntity> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<DepartmentEntity> findByParentIdOrderBySortOrderAscCodeAsc(String parentId);

    List<DepartmentEntity> findByIsActiveTrueOrderByTreeLevelAscSortOrderAscCodeAsc();
}
