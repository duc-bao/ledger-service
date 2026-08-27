package com.ledger.ledgerservice.service.excel;

import com.ledger.ledgerservice.model.dto.excel.ColumnDefinition;
import com.ledger.ledgerservice.model.dto.excel.ExcelTemplateDefinition;
import com.ledger.ledgerservice.model.dto.excel.ExportDateRange;
import com.ledger.ledgerservice.model.dto.excel.UserExportRow;
import com.ledger.ledgerservice.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
@Slf4j
public class UserExcelExportReport extends AbstractExcelExportService<UserExportRow> {
    private final UserRepository userRepository;
    private final ExcelExportReportRegistry registry;

    public UserExcelExportReport(ExcelExportProgressService progressService,
                                 UserRepository userRepository,
                                 ExcelExportReportRegistry registry,
                                 @Value("${excel.export.max-range-days:90}") int maxRangeDays) {
        super(progressService, maxRangeDays);
        this.userRepository = userRepository;
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
        registry.register("USER", this);
    }

    @Override
    public Stream<UserExportRow> streamData(ExportDateRange dateRange) {
        return userRepository.streamForExport(dateRange.getStartDate(), dateRange.getEndDate());
    }

    @Override
    public void writeRow(UserExportRow rowData, ExcelWorkbookWriter writer, ExcelTemplateDefinition template, int rowIndex) {
        for (int i = 0; i < template.getColumns().size(); i++) {
            ColumnDefinition col = template.getColumns().get(i);
            Object val = switch (col.getField()) {
                case "username" -> rowData.getUsername();
                case "fullName" -> rowData.getFullName();
                case "email" -> rowData.getEmail();
                case "phone" -> rowData.getPhone();
                case "userType" -> rowData.getUserType();
                case "status" -> rowData.getStatus();
                case "createdAt" -> rowData.getCreatedAt();
                default -> null;
            };
            writer.writeCell(rowIndex, i, val);
        }
    }
}
