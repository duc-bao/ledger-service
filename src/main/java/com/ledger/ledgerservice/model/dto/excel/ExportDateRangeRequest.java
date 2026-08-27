package com.ledger.ledgerservice.model.dto.excel;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExportDateRangeRequest {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
