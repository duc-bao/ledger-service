package com.ledger.ledgerservice.model.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogExportRow {
    private String requestId;
    private String username;
    private String service;
    private String action;
    private String requestMethod;
    private String requestUrlPath;
    private Integer statusCode;
    private Long durationMs;
    private LocalDateTime createdAt;
}
