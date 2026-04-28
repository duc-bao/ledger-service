package com.ledger.ledgerservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "sys_action_logs",
        indexes = {
                @Index(columnList = "request_id"),
                @Index(columnList = "username"),
                @Index(columnList = "action"),
                @Index(columnList = "status_code"),
                @Index(columnList = "created_at")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ActionLog extends EntityBase {

    @Column(name = "request_id", nullable = false, length = 50)
    private String requestId;

    @Column(name = "span_id", length = 50)
    private String spanId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "service", length = 100)
    private String service;

    @Column(name = "action", length = 200)
    private String action;

    @Column(name = "request_method", length = 16)
    private String requestMethod;

    @Column(name = "request_url", length = 500)
    private String requestUrl;

    @Column(name = "request_url_path", length = 300)
    private String requestUrlPath;

    @Column(name = "request_query", length = 1000)
    private String requestQuery;

    @Column(name = "request_ip", length = 64)
    private String requestIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "error_msg", length = 2000)
    private String errorMsg;

    @Column(name = "request_start")
    private LocalDateTime requestStart;

    @Column(name = "request_end")
    private LocalDateTime requestEnd;

    @Column(name = "duration_ms")
    private Long durationMs;
}
