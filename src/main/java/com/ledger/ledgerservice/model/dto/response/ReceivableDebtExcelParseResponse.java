package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReceivableDebtExcelParseResponse {
    private String sheetName;
    private Integer totalRows;
    private Integer validRows;
    private Integer errorRows;
    private List<ReceivableDebtExcelRowResponse> rows;
    private List<ReceivableDebtExcelValidationErrorResponse> errors;
}
