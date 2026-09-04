package com.ledger.ledgerservice.repository;

import com.ledger.ledgerservice.model.entity.ExcelExportJob;
import com.ledger.ledgerservice.model.enums.ExcelExportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ExcelExportJobRepository extends JpaRepository<ExcelExportJob, String> {

    @Query("SELECT j.status FROM ExcelExportJob j WHERE j.id = :id")
    ExcelExportStatus findStatusById(@Param("id") String id);

    Page<ExcelExportJob> findByRequestedBy(String requestedBy, Pageable pageable);

    Page<ExcelExportJob> findByRequestedByAndReportType(String requestedBy, String reportType, Pageable pageable);

    Page<ExcelExportJob> findByRequestedByAndStatus(String requestedBy, ExcelExportStatus status, Pageable pageable);

    Page<ExcelExportJob> findByRequestedByAndReportTypeAndStatus(String requestedBy, String reportType, ExcelExportStatus status, Pageable pageable);

    Page<ExcelExportJob> findByReportType(String reportType, Pageable pageable);

    Page<ExcelExportJob> findByReportTypeAndStatus(String reportType, ExcelExportStatus status, Pageable pageable);

    Page<ExcelExportJob> findByStatus(ExcelExportStatus status, Pageable pageable);

    List<ExcelExportJob> findByStatus(ExcelExportStatus status);

    List<ExcelExportJob> findByStatusAndStartedAtBefore(ExcelExportStatus status, LocalDateTime threshold);

    List<ExcelExportJob> findByStatusAndExpiredAtBefore(ExcelExportStatus status, LocalDateTime threshold);
}
