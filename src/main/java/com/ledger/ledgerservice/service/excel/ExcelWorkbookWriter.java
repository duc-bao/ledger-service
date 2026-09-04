package com.ledger.ledgerservice.service.excel;

import lombok.Getter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ExcelWorkbookWriter {
    @Getter
    private final SXSSFWorkbook workbook;
    private SXSSFSheet sheet;
    private final Map<String, CellStyle> styleCache = new HashMap<>();

    public ExcelWorkbookWriter(int windowSize) {
        this.workbook = new SXSSFWorkbook(windowSize);
    }

    public void initSheet(String sheetName) {
        this.sheet = this.workbook.createSheet(sheetName);
    }

    public void writeTitle(String titleText, int titleRowIndex, int colCount) {
        if (sheet == null) {
            initSheet("Sheet1");
        }
        SXSSFRow row = sheet.createRow(titleRowIndex);
        row.setHeightInPoints(35);
        SXSSFCell cell = row.createCell(0);
        cell.setCellValue(titleText);

        CellStyle titleStyle = getOrCreateStyle("TITLE", () -> {
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 16);
            font.setBold(true);
            style.setFont(font);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            return style;
        });

        cell.setCellStyle(titleStyle);
        if (colCount > 1) {
            sheet.addMergedRegion(new CellRangeAddress(titleRowIndex, titleRowIndex, 0, colCount - 1));
        }
    }

    public void writeHeaders(java.util.List<String> headers, java.util.List<Integer> columnWidths, int headerRowIndex) {
        if (sheet == null) {
            initSheet("Sheet1");
        }
        SXSSFRow row = sheet.createRow(headerRowIndex);
        row.setHeightInPoints(24);

        CellStyle headerStyle = getOrCreateStyle("HEADER", () -> {
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 11);
            font.setBold(true);
            style.setFont(font);
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorder(style);
            return style;
        });

        for (int i = 0; i < headers.size(); i++) {
            SXSSFCell cell = row.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);

            if (columnWidths != null && i < columnWidths.size()) {
                // Column width is set in characters; POI requires 256 * characters
                sheet.setColumnWidth(i, columnWidths.get(i) * 256);
            } else {
                sheet.setColumnWidth(i, 15 * 256); // default width
            }
        }
    }

    public void writeCell(int rowIndex, int colIndex, Object value) {
        writeCell(rowIndex, colIndex, value, null);
    }

    public void writeCell(int rowIndex, int colIndex, Object value, String formatPattern) {
        if (sheet == null) {
            initSheet("Sheet1");
        }
        SXSSFRow row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        SXSSFCell cell = row.createCell(colIndex);

        if (value == null) {
            cell.setCellValue("");
            cell.setCellStyle(getNormalStyle());
            return;
        }

        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
            if (formatPattern != null) {
                cell.setCellStyle(getFormattedStyle(formatPattern));
            } else {
                cell.setCellStyle(getNormalStyle());
            }
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
            cell.setCellStyle(getNormalStyle());
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
            cell.setCellStyle(getFormattedStyle(formatPattern != null ? formatPattern : "yyyy-MM-dd HH:mm:ss"));
        } else if (value instanceof Calendar) {
            cell.setCellValue((Calendar) value);
            cell.setCellStyle(getFormattedStyle(formatPattern != null ? formatPattern : "yyyy-MM-dd HH:mm:ss"));
        } else if (value instanceof LocalDateTime) {
            String formatted = ((LocalDateTime) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            cell.setCellValue(formatted);
            cell.setCellStyle(getNormalStyle());
        } else if (value instanceof LocalDate) {
            String formatted = ((LocalDate) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            cell.setCellValue(formatted);
            cell.setCellStyle(getNormalStyle());
        } else if (value instanceof Enum<?>) {
            cell.setCellValue(((Enum<?>) value).name());
            cell.setCellStyle(getNormalStyle());
        } else {
            cell.setCellValue(value.toString());
            cell.setCellStyle(getNormalStyle());
        }
    }

    public void writeToFile(File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }
    }

    private CellStyle getNormalStyle() {
        return getOrCreateStyle("NORMAL", () -> {
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            style.setFont(font);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorder(style);
            return style;
        });
    }

    private CellStyle getFormattedStyle(String formatPattern) {
        String cacheKey = "FORMAT_" + formatPattern;
        return getOrCreateStyle(cacheKey, () -> {
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            style.setFont(font);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorder(style);
            DataFormat format = workbook.createDataFormat();
            style.setDataFormat(format.getFormat(formatPattern));
            return style;
        });
    }

    private CellStyle getOrCreateStyle(String key, java.util.function.Supplier<CellStyle> creator) {
        CellStyle style = styleCache.get(key);
        if (style == null) {
            style = creator.get();
            styleCache.put(key, style);
        }
        return style;
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
    }
}
