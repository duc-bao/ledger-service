package com.ledger.ledgerservice.service.excel;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.response.ReceivableDebtExcelParseResponse;
import com.ledger.ledgerservice.model.dto.response.ReceivableDebtExcelRowResponse;
import com.ledger.ledgerservice.model.dto.response.ReceivableDebtExcelValidationErrorResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class ReceivableDebtExcelServiceImpl implements ReceivableDebtExcelService {
    private static final int DATA_START_ROW_INDEX = 8;
    private static final byte[] ZIP_MAGIC_BYTES = new byte[]{0x50, 0x4B, 0x03, 0x04};
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024L; // 20MB

    static {
        // Bảo vệ chống tấn công Zip Bomb trong Apache POI
        ZipSecureFile.setMinInflateRatio(0.01);
        ZipSecureFile.setMaxEntrySize(50 * 1024 * 1024L);
        ZipSecureFile.setMaxTextSize(10 * 1024 * 1024L);
    }

    @Override
    public ReceivableDebtExcelParseResponse parseReceivableDebtReport(MultipartFile file) {
        validateFile(file);

        List<ReceivableDebtExcelRowResponse> validRows = new ArrayList<>();
        List<ReceivableDebtExcelValidationErrorResponse> errors = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                throw new BusinessException(MessageCode.INPUT_INVALID, HttpStatus.BAD_REQUEST);
            }

            int totalRows = 0;
            for (int rowIdx = DATA_START_ROW_INDEX; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (isDataRowEmpty(row)) {
                    continue;
                }
                totalRows++;
                parseRow(row, rowIdx + 1, validRows, errors);
            }

            int errorRows = (int) errors.stream().map(ReceivableDebtExcelValidationErrorResponse::getRowNumber).distinct().count();
            return ReceivableDebtExcelParseResponse.builder()
                    .sheetName(sheet.getSheetName())
                    .totalRows(totalRows)
                    .validRows(validRows.size())
                    .errorRows(errorRows)
                    .rows(validRows)
                    .errors(errors)
                    .build();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to parse receivable debt excel", ex);
            throw new BusinessException(MessageCode.INPUT_INVALID, HttpStatus.BAD_REQUEST);
        }
    }

    private void parseRow(Row row, int excelRowNumber, List<ReceivableDebtExcelRowResponse> validRows,
                          List<ReceivableDebtExcelValidationErrorResponse> errors) {
        List<ReceivableDebtExcelValidationErrorResponse> rowErrors = new ArrayList<>();

        String stt = parseRequiredString(row, 0, "A", "stt", excelRowNumber, rowErrors);
        String customerName = parseRequiredString(row, 1, "B", "customerName", excelRowNumber, rowErrors);

        BigDecimal c2 = parseRequiredNumber(row, 2, "C", "totalBadAndOverdueDebt", excelRowNumber, rowErrors);
        BigDecimal d3 = parseRequiredNumber(row, 3, "D", "badDebt", excelRowNumber, rowErrors);
        BigDecimal e4 = parseRequiredNumber(row, 4, "E", "overdueTotal", excelRowNumber, rowErrors);
        BigDecimal f5 = parseRequiredNumber(row, 5, "F", "overdue6MonthsTo1Year", excelRowNumber, rowErrors);
        BigDecimal g6 = parseRequiredNumber(row, 6, "G", "overdue1To2Years", excelRowNumber, rowErrors);
        BigDecimal h7 = parseRequiredNumber(row, 7, "H", "overdue2To3Years", excelRowNumber, rowErrors);
        BigDecimal i8 = parseRequiredNumber(row, 8, "I", "overdueOver3Years", excelRowNumber, rowErrors);
        BigDecimal j9 = parseRequiredNumber(row, 9, "J", "recoveredQ1", excelRowNumber, rowErrors);
        BigDecimal k10 = parseRequiredNumber(row, 10, "K", "recoveredQ2", excelRowNumber, rowErrors);
        BigDecimal l11 = parseRequiredNumber(row, 11, "L", "recoveredQ3", excelRowNumber, rowErrors);
        BigDecimal m12 = parseRequiredNumber(row, 12, "M", "recoveredQ4", excelRowNumber, rowErrors);
        BigDecimal n13 = parseRequiredNumber(row, 13, "N", "accumulatedOverdue", excelRowNumber, rowErrors);
        BigDecimal o14 = parseRequiredNumber(row, 14, "O", "accumulatedBadDebt", excelRowNumber, rowErrors);
        BigDecimal p15 = parseRequiredNumber(row, 15, "P", "accumulatedTotal", excelRowNumber, rowErrors);

        if (!rowErrors.isEmpty()) {
            errors.addAll(rowErrors);
            return;
        }

        validRows.add(ReceivableDebtExcelRowResponse.builder()
                .rowNumber(excelRowNumber)
                .stt(stt)
                .customerName(customerName)
                .totalBadAndOverdueDebt(c2)
                .badDebt(d3)
                .overdueTotal(e4)
                .overdue6MonthsTo1Year(f5)
                .overdue1To2Years(g6)
                .overdue2To3Years(h7)
                .overdueOver3Years(i8)
                .recoveredQ1(j9)
                .recoveredQ2(k10)
                .recoveredQ3(l11)
                .recoveredQ4(m12)
                .accumulatedOverdue(n13)
                .accumulatedBadDebt(o14)
                .accumulatedTotal(p15)
                .build());
    }

    private String parseRequiredString(Row row, int cellIndex, String column, String field, int rowNumber,
                                       List<ReceivableDebtExcelValidationErrorResponse> rowErrors) {
        Cell cell = row.getCell(cellIndex, MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            rowErrors.add(buildError(rowNumber, column, field, "Giá trị bắt buộc"));
            return null;
        }

        String value = switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception ex) {
                    yield null;
                }
            }
            default -> null;
        };

        if (value == null || value.trim().isEmpty()) {
            rowErrors.add(buildError(rowNumber, column, field, "Giá trị bắt buộc"));
            return null;
        }
        return value.trim();
    }

    private BigDecimal parseRequiredNumber(Row row, int cellIndex, String column, String field, int rowNumber,
                                           List<ReceivableDebtExcelValidationErrorResponse> rowErrors) {
        Cell cell = row.getCell(cellIndex, MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            rowErrors.add(buildError(rowNumber, column, field, "Giá trị số bắt buộc"));
            return null;
        }

        try {
            BigDecimal value;
            if (cell.getCellType() == CellType.NUMERIC) {
                value = BigDecimal.valueOf(cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                String raw = cell.getStringCellValue();
                if (raw == null || raw.trim().isEmpty()) {
                    rowErrors.add(buildError(rowNumber, column, field, "Giá trị số bắt buộc"));
                    return null;
                }
                String normalized = raw.trim()
                        .replace(" ", "")
                        .replace(".", "")
                        .replace(",", ".");
                value = new BigDecimal(normalized);
            } else if (cell.getCellType() == CellType.FORMULA) {
                value = BigDecimal.valueOf(cell.getNumericCellValue());
            } else {
                rowErrors.add(buildError(rowNumber, column, field, "Không đúng định dạng số"));
                return null;
            }

            if (value.compareTo(BigDecimal.ZERO) < 0) {
                rowErrors.add(buildError(rowNumber, column, field, "Không được âm"));
            }
            return value;
        } catch (Exception ex) {
            rowErrors.add(buildError(rowNumber, column, field, "Không đúng định dạng số"));
            return null;
        }
    }

    private boolean isDataRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int i = 0; i <= 15; i++) {
            Cell cell = row.getCell(i, MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell == null) {
                continue;
            }
            if (cell.getCellType() == CellType.BLANK) {
                continue;
            }
            if (cell.getCellType() == CellType.STRING && cell.getStringCellValue() != null
                    && !cell.getStringCellValue().trim().isEmpty()) {
                return false;
            }
            if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA || cell.getCellType() == CellType.BOOLEAN) {
                return false;
            }
        }
        return true;
    }

    private ReceivableDebtExcelValidationErrorResponse buildError(int rowNumber, String column, String field, String message) {
        return ReceivableDebtExcelValidationErrorResponse.builder()
                .rowNumber(rowNumber)
                .column(column)
                .field(field)
                .message(message)
                .build();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(MessageCode.EXCEL_FILE_EMPTY, HttpStatus.BAD_REQUEST);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(MessageCode.EXCEL_FILE_INVALID_TYPE, HttpStatus.BAD_REQUEST);
        }

        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw new BusinessException(MessageCode.EXCEL_FILE_INVALID_TYPE, HttpStatus.BAD_REQUEST);
        }

        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            int read = is.read(header);
            if (read < 4 || header[0] != ZIP_MAGIC_BYTES[0] || header[1] != ZIP_MAGIC_BYTES[1]
                    || header[2] != ZIP_MAGIC_BYTES[2] || header[3] != ZIP_MAGIC_BYTES[3]) {
                throw new BusinessException(MessageCode.EXCEL_FILE_INVALID_TYPE, HttpStatus.BAD_REQUEST);
            }
        } catch (IOException ex) {
            throw new BusinessException(MessageCode.EXCEL_FILE_INVALID_TYPE, HttpStatus.BAD_REQUEST);
        }
    }
}
