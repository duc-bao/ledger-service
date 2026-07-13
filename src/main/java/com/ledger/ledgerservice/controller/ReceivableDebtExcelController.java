package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.ReceivableDebtExcelParseResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.excel.ReceivableDebtExcelService;
import com.ledger.ledgerservice.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/excel/receivable-debt")
@RequiredArgsConstructor
@Tag(name = "Receivable Debt Excel", description = "APIs for parsing receivable debt overdue report excel")
@SecurityRequirement(name = "bearerAuth")
public class ReceivableDebtExcelController {
    private final ReceivableDebtExcelService receivableDebtExcelService;
    private final ResponseHelper responseHelper;

    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Parse receivable debt report", description = "Upload .xlsx report and return parsed rows with validation errors")
    public ResponseEntity<BaseResponse<ReceivableDebtExcelParseResponse>> parseReport(
            @Parameter(description = "Excel file (.xlsx)", required = true)
            @RequestPart("file") MultipartFile file) {
        ReceivableDebtExcelParseResponse data = receivableDebtExcelService.parseReceivableDebtReport(file);
        return responseHelper.ok(MessageCode.SUCCESS, data, HttpStatus.OK);
    }
}
