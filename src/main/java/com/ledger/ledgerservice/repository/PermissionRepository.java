package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, String> {
    List<Permission> findByUserId(String userId);

    List<Permission> findByGroupIdIn(Collection<String> groupIds);
    List<Permission> findByGroupId(String groupId);
    void deleteByGroupId(String groupId);
}
