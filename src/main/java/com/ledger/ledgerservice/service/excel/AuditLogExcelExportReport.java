package com.ledger.ledgerservice.service.excel;

import com.ledger.ledgerservice.model.dto.excel.AuditLogExportRow;
import com.ledger.ledgerservice.model.dto.excel.ColumnDefinition;
import com.ledger.ledgerservice.model.dto.excel.ExcelTemplateDefinition;
import com.ledger.ledgerservice.model.dto.excel.ExportDateRange;
import com.ledger.ledgerservice.repository.ActionLogRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
@Slf4j
public class AuditLogExcelExportReport extends AbstractExcelExportService<AuditLogExportRow> {
    private final ActionLogRepository actionLogRepository;
    private final ExcelExportReportRegistry registry;

    public AuditLogExcelExportReport(ExcelExportProgressService progressService,
                                     ActionLogRepository actionLogRepository,
                                     ExcelExportReportRegistry registry,
                                     @Value("${excel.export.max-range-days:90}") int maxRangeDays) {
        super(progressService, maxRangeDays);
        this.actionLogRepository = actionLogRepository;
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
        registry.register("AUDIT_LOG", this);
    }

    @Override
    public Stream<AuditLogExportRow> streamData(ExportDateRange dateRange) {
        return actionLogRepository.streamForExport(dateRange.getStartDate(), dateRange.getEndDate());
    }

    @Override
    public void writeRow(AuditLogExportRow rowData, ExcelWorkbookWriter writer, ExcelTemplateDefinition template, int rowIndex) {
        for (int i = 0; i < template.getColumns().size(); i++) {
            ColumnDefinition col = template.getColumns().get(i);
            Object val = switch (col.getField()) {
                case "requestId" -> rowData.getRequestId();
                case "username" -> rowData.getUsername();
                case "service" -> rowData.getService();
                case "action" -> rowData.getAction();
                case "requestMethod" -> rowData.getRequestMethod();
                case "requestUrlPath" -> rowData.getRequestUrlPath();
                case "statusCode" -> rowData.getStatusCode();
                case "durationMs" -> rowData.getDurationMs();
                case "createdAt" -> rowData.getCreatedAt();
                default -> null;
            };
            writer.writeCell(rowIndex, i, val);
        }
    }
}
