package com.ledger.ledgerservice.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditLogResponse {
    private String id;

    private String requestId;

    private String spanId;

    private String username;

    private String service;

    private String action;

    private String requestMethod;

    private String requestUrl;

    private String requestUrlPath;

    private String requestQuery;

    private String requestIp;

    private String userAgent;

    private Integer statusCode;

    private String errorCode;

    private String errorMsg;

    private LocalDateTime requestStart;

    private LocalDateTime requestEnd;

    private Long durationMs;
}
