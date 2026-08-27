package com.ledger.ledgerservice.service.excel;

import com.ledger.ledgerservice.model.dto.excel.ExportDateRangeRequest;
import com.ledger.ledgerservice.model.dto.excel.ExportJobResponse;
import com.ledger.ledgerservice.model.enums.ExcelExportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExcelExportJobService {
    ExportJobResponse requestExport(String reportType, ExportDateRangeRequest request);

    ExportJobResponse getJobStatus(String jobId);

    Page<ExportJobResponse> searchJobs(String reportType, ExcelExportStatus status, Pageable pageable);

    String downloadFile(String jobId);

    void cancelJob(String jobId);
}
