package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReceivableDebtExcelValidationErrorResponse {
    private Integer rowNumber;
    private String column;
    private String field;
    private String message;
}
