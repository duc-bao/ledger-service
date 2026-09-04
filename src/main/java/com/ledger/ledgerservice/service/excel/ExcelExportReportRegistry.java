package com.ledger.ledgerservice.service.excel;

import com.ledger.ledgerservice.exception.BusinessExportException;
import com.ledger.ledgerservice.model.enums.MessageCode;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ExcelExportReportRegistry {
    private final Map<String, AbstractExcelExportService<?>> registry = new ConcurrentHashMap<>();

    public void register(String reportType, AbstractExcelExportService<?> reportService) {
        registry.put(reportType.toUpperCase(), reportService);
    }

    public AbstractExcelExportService<?> getReportService(String reportType) {
        AbstractExcelExportService<?> service = registry.get(reportType.toUpperCase());
        if (service == null) {
            throw new BusinessExportException(MessageCode.EXCEL_EXPORT_REPORT_NOT_SUPPORTED, reportType);
        }
        return service;
    }
}
