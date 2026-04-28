package com.ledger.ledgerservice.model.dto.request.excel;

import lombok.Data;

import java.util.List;

@Data
public class BusinessReportTemplateConfig {
    private String sheetName;
    private String title;
    private Integer titleRowIndex;
    private Integer headerRowIndex;
    private List<String> headers;
    private List<Integer> columnWidths;
}
