package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.ExcelExportJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExcelExportJobRepository extends JpaRepository<ExcelExportJob, String> {
}
