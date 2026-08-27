package com.ledger.ledgerservice.model.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnDefinition {
    private String header;
    private String field;
    private int width;
}
