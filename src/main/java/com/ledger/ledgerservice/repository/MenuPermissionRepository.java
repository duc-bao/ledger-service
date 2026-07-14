package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.MenuPermission;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MenuPermissionRepository extends JpaRepository<MenuPermission, String> {
    boolean existsByMenuIdAndPermissionId(String menuId, String permissionId);

    boolean existsByPermissionId(String permissionId);

    List<MenuPermission> findByPermissionIdIn(Collection<String> permissionIds);

    List<MenuPermission> findByMenuId(String menuId);

    List<MenuPermission> findByMenuIdAndStatus(String menuId, RecordStatus status);

    List<MenuPermission> findByMenuIdIn(Collection<String> menuIds);

    Optional<MenuPermission> findByIdAndStatus(String id, RecordStatus status);
}