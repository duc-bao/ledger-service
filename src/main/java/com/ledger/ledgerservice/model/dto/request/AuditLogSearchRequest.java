package com.ledger.ledgerservice.model.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogSearchRequest {
    @Size(max = 200)
    private String keyword;

    @Size(max = 50)
    private String requestId;

    @Size(max = 100)
    private String username;

    @Size(max = 200)
    private String action;

    @Size(max = 16)
    private String requestMethod;

    @Min(100)
    @Max(599)
    private Integer statusCode;

    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    @Min(0)
    private Integer page = 0;

    @Min(1)
    @Max(100)
    private Integer size = 20;

    @Pattern(regexp = "^(requestStart|durationMs|statusCode|username|action)$", message = "error.invalid")
    private String sortBy = "requestStart";

    @Pattern(regexp = "^(?i)(ASC|DESC)$", message = "error.invalid")
    private String sortDir = "DESC";

    @AssertTrue(message = "error.searchDateRange.invalid")
    public boolean isDateRangeValid() {
        if (fromDate == null || toDate == null) {
            return true;
        }
        return !fromDate.isAfter(toDate);
    }
}
