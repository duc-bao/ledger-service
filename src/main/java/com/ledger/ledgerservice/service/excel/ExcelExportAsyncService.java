package com.ledger.ledgerservice.service.excel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.ledgerservice.config.properties.AppSettingProperty;
import com.ledger.ledgerservice.model.dto.request.excel.BusinessReportTemplateConfig;
import com.ledger.ledgerservice.model.entity.ExcelExportJob;
import com.ledger.ledgerservice.model.enums.ExcelExportStatus;
import com.ledger.ledgerservice.repository.ExcelExportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelExportAsyncService {
    private static final String DEFAULT_EXPORT_DIR = "./storage/excel/exports";
    private static final String TEMPLATE_PATH = "excel-template/business-report-template.json";
    private static final DateTimeFormatter FILE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ExcelExportJobRepository excelExportJobRepository;
    private final AppSettingProperty appSettingProperty;
    private final ObjectMapper objectMapper;

    @Async("statisticsExecutor")
    public void processBusinessReportExport(String jobId) {
        ExcelExportJob job = excelExportJobRepository.findById(jobId).orElse(null);
        if (job == null) {
            return;
        }

        try {
            job.setStatus(ExcelExportStatus.PROCESSING);
            job.setUpdatedAt(LocalDateTime.now());
            excelExportJobRepository.save(job);

            BusinessReportTemplateConfig config = loadTemplateConfig();
            Path exportDir = resolveExportDir();
            Files.createDirectories(exportDir);
            String fileName = buildOutputFileName();
            Path filePath = exportDir.resolve(fileName).normalize();

            try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
                 OutputStream outputStream = Files.newOutputStream(filePath)) {
                workbook.setCompressTempFiles(true);
                createTemplate(workbook, config);
                workbook.write(outputStream);
                workbook.dispose();
            }

            job.setStatus(ExcelExportStatus.COMPLETED);
            job.setFileName(fileName);
            job.setFilePath(filePath.toAbsolutePath().toString());
            job.setErrorMessage(null);
            job.setCompletedAt(LocalDateTime.now());
            job.setUpdatedAt(LocalDateTime.now());
            excelExportJobRepository.save(job);
        } catch (Exception ex) {
            log.error("Failed to export excel jobId={}", jobId, ex);
            job.setStatus(ExcelExportStatus.FAILED);
            job.setErrorMessage(ex.getMessage());
            job.setCompletedAt(LocalDateTime.now());
            job.setUpdatedAt(LocalDateTime.now());
            excelExportJobRepository.save(job);
        }
    }

    private BusinessReportTemplateConfig loadTemplateConfig() throws Exception {
        ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);
        try (InputStream is = resource.getInputStream()) {
            return objectMapper.readValue(is, BusinessReportTemplateConfig.class);
        }
    }

    private void createTemplate(SXSSFWorkbook workbook, BusinessReportTemplateConfig config) {
        List<String> headers = config.getHeaders();
        if (headers == null || headers.isEmpty()) {
            throw new IllegalStateException("Template headers are empty");
        }

        Sheet sheet = workbook.createSheet(StringUtils.hasText(config.getSheetName()) ? config.getSheetName() : "Sheet1");
        int titleRowIndex = config.getTitleRowIndex() != null ? config.getTitleRowIndex() : 0;
        int headerRowIndex = config.getHeaderRowIndex() != null ? config.getHeaderRowIndex() : 2;

        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        Row titleRow = sheet.createRow(titleRowIndex);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(config.getTitle());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(titleRowIndex, titleRowIndex, 0, headers.size() - 1));

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        Row headerRow = sheet.createRow(headerRowIndex);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }

        List<Integer> widths = config.getColumnWidths();
        for (int i = 0; i < headers.size(); i++) {
            int width = 18;
            if (widths != null && i < widths.size() && widths.get(i) != null && widths.get(i) > 0) {
                width = widths.get(i);
            }
            sheet.setColumnWidth(i, width * 256);
        }
    }

    private Path resolveExportDir() {
        String configured = appSettingProperty.getTempDirectory();
        String baseDir = StringUtils.hasText(configured) ? configured.trim() : DEFAULT_EXPORT_DIR;
        return Paths.get(baseDir).toAbsolutePath().normalize();
    }

    private String buildOutputFileName() {
        return "bao-cao-ket-qua-hoat-dong-kinh-doanh_"
                + FILE_TIME_FORMAT.format(LocalDateTime.now())
                + "_"
                + UUID.randomUUID()
                + ".xlsx";
    }
}
