package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.DepartmentPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DepartmentPermissionRepository extends JpaRepository<DepartmentPermissionEntity, String> {
    List<DepartmentPermissionEntity> findByDepartmentId(String departmentId);

    List<DepartmentPermissionEntity> findByDepartmentIdIn(Collection<String> departmentIds);

    List<DepartmentPermissionEntity> findByMenuId(String menuId);

    void deleteByDepartmentId(String departmentId);
}
