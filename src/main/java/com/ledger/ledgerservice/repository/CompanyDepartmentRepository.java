package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.CompanyDepartment;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyDepartmentRepository extends JpaRepository<CompanyDepartment, String> {
    List<CompanyDepartment> findByCompanyId(String companyId);
    List<CompanyDepartment> findByCompanyIdAndStatus(String companyId, RecordStatus status);
    Optional<CompanyDepartment> findByCompanyIdAndDepartmentId(String companyId, String departmentId);
}
