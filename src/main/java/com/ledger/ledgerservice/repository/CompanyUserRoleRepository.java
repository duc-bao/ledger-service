package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.CompanyUserRole;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyUserRoleRepository extends JpaRepository<CompanyUserRole, String> {
    boolean existsByCompanyIdAndUserIdAndGroupId(String companyId, String userId, String groupId);

    List<CompanyUserRole> findByCompanyIdAndUserIdAndStatus(String companyId, String userId, RecordStatus status);

    List<CompanyUserRole> findByCompanyIdAndStatus(String companyId, RecordStatus status);

    List<CompanyUserRole> findByGroupIdAndStatus(String groupId, RecordStatus status);
}
