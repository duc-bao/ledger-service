package com.ledger.ledgerservice.model.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestContext {
    @Builder.Default
    private Boolean execute = false;
    private String requestId;
    private String spanId;
    private String requestIp;
    private String requestMethod;
    private String requestUrl;
    private String requestQuery;
    private String requestUrlPath;
    private String userAgent;
    private String userId;
    private String username;
    private LocalDateTime requestStart;
    private LocalDateTime requestEnd;
    private Integer statusCode;
    private String errorCode;
    private String errorMsg;
    private Long durationMs;
    private String service;
    private String action;
}