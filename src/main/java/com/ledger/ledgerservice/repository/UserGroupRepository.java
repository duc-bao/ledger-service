package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserGroupRepository extends JpaRepository<UserGroup, String> {
    List<UserGroup> findByUserId(String userId);
    List<UserGroup> findByGroupId(String groupId);
    void deleteByUserId(String userId);
}
