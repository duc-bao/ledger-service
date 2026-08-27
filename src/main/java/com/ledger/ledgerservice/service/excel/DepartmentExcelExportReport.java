package com.ledger.ledgerservice.service.excel;

import com.ledger.ledgerservice.model.dto.excel.ColumnDefinition;
import com.ledger.ledgerservice.model.dto.excel.DepartmentExportRow;
import com.ledger.ledgerservice.model.dto.excel.ExcelTemplateDefinition;
import com.ledger.ledgerservice.model.dto.excel.ExportDateRange;
import com.ledger.ledgerservice.repository.DepartmentRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
@Slf4j
public class DepartmentExcelExportReport extends AbstractExcelExportService<DepartmentExportRow> {
    private final DepartmentRepository departmentRepository;
    private final ExcelExportReportRegistry registry;

    public DepartmentExcelExportReport(ExcelExportProgressService progressService,
                                       DepartmentRepository departmentRepository,
                                       ExcelExportReportRegistry registry,
                                       @Value("${excel.export.max-range-days:90}") int maxRangeDays) {
        super(progressService, maxRangeDays);
        this.departmentRepository = departmentRepository;
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
        registry.register("DEPARTMENT", this);
    }

    @Override
    public Stream<DepartmentExportRow> streamData(ExportDateRange dateRange) {
        return departmentRepository.streamForExport(dateRange.getStartDate(), dateRange.getEndDate());
    }

    @Override
    public void writeRow(DepartmentExportRow rowData, ExcelWorkbookWriter writer, ExcelTemplateDefinition template, int rowIndex) {
        for (int i = 0; i < template.getColumns().size(); i++) {
            ColumnDefinition col = template.getColumns().get(i);
            Object val = switch (col.getField()) {
                case "code" -> rowData.getCode();
                case "name" -> rowData.getName();
                case "shortName" -> rowData.getShortName();
                case "parentId" -> rowData.getParentId();
                case "status" -> rowData.getStatus();
                case "isActive" -> rowData.getIsActive();
                case "createdAt" -> rowData.getCreatedAt();
                default -> null;
            };
            writer.writeCell(rowIndex, i, val);
        }
    }
}
