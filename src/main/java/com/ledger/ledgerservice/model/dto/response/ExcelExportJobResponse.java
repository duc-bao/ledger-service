package com.ledger.ledgerservice.model.dto.response;

import com.ledger.ledgerservice.model.enums.ExcelExportStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ExcelExportJobResponse {
    private String jobId;
    private String reportType;
    private ExcelExportStatus status;
    private String fileName;
    private String requestedBy;
    private LocalDateTime requestedAt;
}
