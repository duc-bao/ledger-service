package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.DepartmentUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentUserRepository extends JpaRepository<DepartmentUserEntity, String> {
    List<DepartmentUserEntity> findByDepartmentId(String departmentId);

    List<DepartmentUserEntity> findByUserId(String userId);

    Optional<DepartmentUserEntity> findByDepartmentIdAndUserId(String departmentId, String userId);

    boolean existsByDepartmentIdAndUserId(String departmentId, String userId);

    void deleteByDepartmentId(String departmentId);

    void deleteByUserId(String userId);
}
