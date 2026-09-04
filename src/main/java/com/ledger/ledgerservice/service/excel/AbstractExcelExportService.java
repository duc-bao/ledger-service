package com.ledger.ledgerservice.service.excel;

import com.ledger.ledgerservice.exception.BusinessExportException;
import com.ledger.ledgerservice.model.dto.excel.ColumnDefinition;
import com.ledger.ledgerservice.model.dto.excel.ExcelTemplateDefinition;
import com.ledger.ledgerservice.model.dto.excel.ExportDateRange;
import com.ledger.ledgerservice.model.dto.excel.ExportExecutionContext;
import com.ledger.ledgerservice.model.enums.MessageCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public abstract class AbstractExcelExportService<T> implements ExcelExportReport<T> {
    private static final int DEFAULT_RANGE_DAYS = 30;

    private final ExcelExportProgressService progressService;
    private final int maxRangeDays;

    protected AbstractExcelExportService(ExcelExportProgressService progressService, int maxRangeDays) {
        this.progressService = progressService;
        this.maxRangeDays = maxRangeDays;
    }

    @Override
    public ExportDateRange normalizeDateRange(ExportDateRange requestedRange) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = requestedRange != null ? requestedRange.getStartDate() : null;
        LocalDateTime endDate = requestedRange != null ? requestedRange.getEndDate() : null;

        if (startDate == null && endDate == null) {
            endDate = now;
            startDate = endDate.minusDays(DEFAULT_RANGE_DAYS);
        } else if (startDate != null && endDate == null) {
            endDate = startDate.plusDays(DEFAULT_RANGE_DAYS);
            if (endDate.isAfter(now)) {
                endDate = now;
            }
        } else if (startDate == null) {
            startDate = endDate.minusDays(DEFAULT_RANGE_DAYS);
        }

        if (startDate.isAfter(endDate)) {
            throw new BusinessExportException(MessageCode.START_DATE_EXCEEDS_END_DATE);
        }

        if (startDate.plusDays(maxRangeDays).isBefore(endDate)) {
            throw new BusinessExportException(MessageCode.SEARCH_DATE_RANGE_INVALID);
        }

        return ExportDateRange.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @Transactional(readOnly = true)
    public void executeExport(ExportExecutionContext context) throws Exception {
        ExcelTemplateDefinition template = context.getTemplate();
        ExcelWorkbookWriter writer = new ExcelWorkbookWriter(500);
        File tempFile = null;

        try {
            tempFile = File.createTempFile("export-" + context.getJobId() + "-", ".xlsx");
            context.setTempFile(tempFile);

            writer.initSheet(template.getSheetName());

            int colCount = template.getColumns().size();
            writer.writeTitle(template.getTitle(), template.getTitleRowIndex(), colCount);

            List<String> headers = template.getColumns().stream()
                    .map(ColumnDefinition::getHeader)
                    .collect(Collectors.toList());
            List<Integer> widths = template.getColumns().stream()
                    .map(ColumnDefinition::getWidth)
                    .collect(Collectors.toList());
            writer.writeHeaders(headers, widths, template.getHeaderRowIndex());

            int dataStartRow = template.getHeaderRowIndex() + 1;
            long count = 0;
            ExportDateRange dateRange = ExportDateRange.builder()
                    .startDate(context.getStartDate())
                    .endDate(context.getEndDate())
                    .build();

            try (Stream<T> dataStream = streamData(dateRange)) {
                Iterable<T> iterable = dataStream::iterator;
                for (T rowData : iterable) {
                    if (count % 1000 == 0) {
                        if (progressService.isCancelled(context.getJobId())) {
                            log.info("Job {} has been cancelled. Aborting export.", context.getJobId());
                            throw new BusinessExportException(MessageCode.EXCEL_EXPORT_CANCELLED);
                        }
                        progressService.updateProgress(context.getJobId(), count);
                    }

                    int currentRowIndex = dataStartRow + (int) count;
                    writeRow(rowData, writer, template, currentRowIndex);
                    count++;
                }
            }

            progressService.updateProgress(context.getJobId(), count);
            context.setTotalRows(count);
            writer.writeToFile(tempFile);
            log.info("Finished writing Excel workbook for job {} to temp file: {}", context.getJobId(), tempFile.getAbsolutePath());

        } finally {
            if (writer.getWorkbook() != null) {
                try {
                    writer.getWorkbook().close();
                    writer.getWorkbook().dispose();
                } catch (IOException e) {
                    log.error("Failed to dispose workbook for job {}", context.getJobId(), e);
                }
            }
        }
    }
}
