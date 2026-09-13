package com.ledger.ledgerservice.model.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogItemResponse {
    private String requestId;
    private String username;
    private String fullName;
    private String action;
    private String description;
    private String requestMethod;
    private String requestUrlPath;
    private String requestIp;
    private Integer statusCode;
    private String result;
    private String errorCode;
    private String errorMsg;
    private LocalDateTime requestStart;
    private LocalDateTime requestEnd;
    private Long durationMs;
}
