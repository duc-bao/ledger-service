package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.ExcelExportJobResponse;
import com.ledger.ledgerservice.model.dto.response.ExcelUploadResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.excel.ExcelService;
import com.ledger.ledgerservice.service.excel.ExportFilePayload;
import com.ledger.ledgerservice.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/excel")
@RequiredArgsConstructor
@Tag(name = "Excel", description = "APIs for Excel upload and export")
@SecurityRequirement(name = "bearerAuth")
public class ExcelController {
    private final ExcelService excelService;
    private final ResponseHelper responseHelper;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload Excel file", description = "Upload an Excel file and process business data")
    public ResponseEntity<BaseResponse<ExcelUploadResponse>> uploadExcel(@Parameter(description = "Input Excel file", required = true) @RequestPart("file") MultipartFile file) {
        ExcelUploadResponse data = excelService.uploadExcel(file);
        return responseHelper.ok(MessageCode.EXCEL_UPLOAD_SUCCESS, data, HttpStatus.OK);
    }

    @PostMapping("/exports/business-report")
    @Operation(summary = "Request business report export", description = "Create an asynchronous export job for business reports")
    public ResponseEntity<BaseResponse<ExcelExportJobResponse>> requestBusinessReportExport() {
        ExcelExportJobResponse data = excelService.requestBusinessReportExport();
        return responseHelper.ok(MessageCode.EXCEL_EXPORT_REQUEST_ACCEPTED, data, HttpStatus.ACCEPTED);
    }

    @GetMapping("/exports/{jobId}/download")
    @Operation(summary = "Download exported file by job", description = "Return the XLSX file when the export job is completed")
    public ResponseEntity<org.springframework.core.io.Resource> downloadExportedFile(@PathVariable String jobId) {
        ExportFilePayload payload = excelService.downloadExportedFile(jobId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(payload.getContentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(payload.getFileName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(payload.getResource());
    }
}
