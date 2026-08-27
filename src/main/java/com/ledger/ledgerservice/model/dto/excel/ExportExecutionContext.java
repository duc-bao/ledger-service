package com.ledger.ledgerservice.model.dto.excel;

import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Locale;

@Data
@Builder
public class ExportExecutionContext {
    private String jobId;
    private String reportType;
    private String requestedBy;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ExcelTemplateDefinition template;
    private File tempFile;
    private Locale locale;
    private long totalRows;
}
