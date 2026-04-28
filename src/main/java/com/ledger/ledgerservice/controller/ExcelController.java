package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.ExcelExportJobResponse;
import com.ledger.ledgerservice.model.dto.response.ExcelUploadResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.excel.ExcelService;
import com.ledger.ledgerservice.service.excel.ExportFilePayload;
import com.ledger.ledgerservice.util.ResponseHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/excel")
@RequiredArgsConstructor
public class ExcelController {
    private final ExcelService excelService;
    private final ResponseHelper responseHelper;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<ExcelUploadResponse>> uploadExcel(@RequestPart("file") MultipartFile file) {
        ExcelUploadResponse data = excelService.uploadExcel(file);
        return responseHelper.ok(MessageCode.EXCEL_UPLOAD_SUCCESS, data, HttpStatus.OK);
    }

    @PostMapping("/exports/business-report")
    public ResponseEntity<BaseResponse<ExcelExportJobResponse>> requestBusinessReportExport() {
        ExcelExportJobResponse data = excelService.requestBusinessReportExport();
        return responseHelper.ok(MessageCode.EXCEL_EXPORT_REQUEST_ACCEPTED, data, HttpStatus.ACCEPTED);
    }

    @GetMapping("/exports/{jobId}/download")
    public ResponseEntity<byte[]> downloadExportedFile(@PathVariable String jobId) {
        ExportFilePayload payload = excelService.downloadExportedFile(jobId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(payload.getFileName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(payload.getContent());
    }
}
