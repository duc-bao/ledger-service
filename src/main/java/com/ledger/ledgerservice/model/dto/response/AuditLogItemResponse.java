package com.ledger.ledgerservice.model.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogItemResponse {
    private String requestId;
    private String username;
    private String action;
    private String requestMethod;
    private String requestUrlPath;
    private Integer statusCode;
    private String errorCode;
    private String errorMsg;
    private LocalDateTime requestStart;
    private LocalDateTime requestEnd;
    private Long durationMs;
}
