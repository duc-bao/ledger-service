package com.ledger.ledgerservice.service.excel;

import com.ledger.ledgerservice.model.dto.excel.ExcelTemplateDefinition;
import com.ledger.ledgerservice.model.dto.excel.ExportDateRange;

import java.util.stream.Stream;

public interface ExcelExportReport<T> {
    ExportDateRange normalizeDateRange(ExportDateRange requestedRange);

    Stream<T> streamData(ExportDateRange dateRange);

    void writeRow(T rowData, ExcelWorkbookWriter writer, ExcelTemplateDefinition template, int rowIndex);
}
