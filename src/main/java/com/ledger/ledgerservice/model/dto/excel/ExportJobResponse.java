package com.ledger.ledgerservice.model.dto.excel;

import com.ledger.ledgerservice.model.enums.ExcelExportStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ExportJobResponse {
    private String id;
    private String reportType;
    private ExcelExportStatus status;
    private String statusUrl;
    private Long totalRows;
    private Long processedRows;
    private Integer progressPercent;
    private String fileName;
    private String errorMessage;
    private LocalDateTime requestedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime expiredAt;
}
