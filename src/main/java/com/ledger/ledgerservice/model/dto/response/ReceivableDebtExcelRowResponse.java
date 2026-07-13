package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ReceivableDebtExcelRowResponse {
    private Integer rowNumber;
    private String stt;
    private String customerName;
    private BigDecimal totalBadAndOverdueDebt;
    private BigDecimal badDebt;
    private BigDecimal overdueTotal;
    private BigDecimal overdue6MonthsTo1Year;
    private BigDecimal overdue1To2Years;
    private BigDecimal overdue2To3Years;
    private BigDecimal overdueOver3Years;
    private BigDecimal recoveredQ1;
    private BigDecimal recoveredQ2;
    private BigDecimal recoveredQ3;
    private BigDecimal recoveredQ4;
    private BigDecimal accumulatedOverdue;
    private BigDecimal accumulatedBadDebt;
    private BigDecimal accumulatedTotal;
}
