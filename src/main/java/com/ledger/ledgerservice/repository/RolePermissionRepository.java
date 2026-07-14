package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.RolePermission;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RolePermissionRepository extends JpaRepository<RolePermission, String> {
    List<RolePermission> findByGroupId(String groupId);

    List<RolePermission> findByGroupIdAndStatus(String groupId, RecordStatus status);

    List<RolePermission> findByGroupIdIn(Collection<String> groupIds);

    Optional<RolePermission> findByGroupIdAndPermissionId(String groupId, String permissionId);

    boolean existsByGroupIdAndPermissionId(String groupId, String permissionId);

    boolean existsByPermissionId(String permissionId);

    void deleteByGroupId(String groupId);
}