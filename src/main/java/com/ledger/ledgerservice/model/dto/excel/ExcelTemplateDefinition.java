package com.ledger.ledgerservice.model.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelTemplateDefinition {
    private String sheetName;
    private String title;
    private int titleRowIndex = 0;
    private int headerRowIndex = 2;
    private List<ColumnDefinition> columns;
}
