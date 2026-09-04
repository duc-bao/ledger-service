package com.ledger.ledgerservice.model.dto.excel;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ExportDateRange {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
