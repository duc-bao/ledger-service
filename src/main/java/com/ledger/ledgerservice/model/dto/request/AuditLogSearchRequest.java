package com.ledger.ledgerservice.model.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class AuditLogSearchRequest extends SearchBaseRequest {
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

    @AssertTrue(message = "error.searchDateRange.invalid")
    public boolean isDateRangeValid() {
        if (fromDate == null || toDate == null) {
            return true;
        }
        return !fromDate.isAfter(toDate);
    }
}
