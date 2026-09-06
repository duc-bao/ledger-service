package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.excel.ExportDateRangeRequest;
import com.ledger.ledgerservice.model.dto.excel.ExportJobResponse;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.enums.ExcelExportStatus;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.excel.ExcelExportJobService;
import com.ledger.ledgerservice.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import com.ledger.ledgerservice.util.PageableUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/exports")
@RequiredArgsConstructor
@Tag(name = "Excel Export", description = "APIs for Excel asynchronous report exports")
@SecurityRequirement(name = "bearerAuth")
public class ExportController {
    private final ExcelExportJobService jobService;
    private final ResponseHelper responseHelper;

    @PostMapping("/users")
    @Operation(summary = "Export users list", description = "Create an asynchronous export job for user reports")
    public ResponseEntity<BaseResponse<ExportJobResponse>> exportUsers(@RequestBody(required = false) ExportDateRangeRequest request) {
        ExportJobResponse data = jobService.requestExport("USER", request);
        return responseHelper.ok(MessageCode.EXCEL_EXPORT_REQUEST_ACCEPTED, data, HttpStatus.ACCEPTED);
    }

    @PostMapping("/departments")
    @Operation(summary = "Export departments list", description = "Create an asynchronous export job for department reports")
    public ResponseEntity<BaseResponse<ExportJobResponse>> exportDepartments(@RequestBody(required = false) ExportDateRangeRequest request) {
        ExportJobResponse data = jobService.requestExport("DEPARTMENT", request);
        return responseHelper.ok(MessageCode.EXCEL_EXPORT_REQUEST_ACCEPTED, data, HttpStatus.ACCEPTED);
    }

    @PostMapping("/audit-logs")
    @Operation(summary = "Export audit logs list", description = "Create an asynchronous export job for audit log reports")
    public ResponseEntity<BaseResponse<ExportJobResponse>> exportAuditLogs(@RequestBody(required = false) ExportDateRangeRequest request) {
        ExportJobResponse data = jobService.requestExport("AUDIT_LOG", request);
        return responseHelper.ok(MessageCode.EXCEL_EXPORT_REQUEST_ACCEPTED, data, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Get export job status", description = "Retrieve progress and status of a specific export job")
    public ResponseEntity<BaseResponse<ExportJobResponse>> getJobStatus(@PathVariable String jobId) {
        ExportJobResponse data = jobService.getJobStatus(jobId);
        return responseHelper.ok(MessageCode.SUCCESS, data);
    }

    @GetMapping
    @Operation(summary = "Search export jobs", description = "Search and list export jobs with filters")
    public ResponseEntity<BaseResponse<Page<ExportJobResponse>>> searchJobs(
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) ExcelExportStatus status,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<ExportJobResponse> data = jobService.searchJobs(reportType, status, PageableUtil.clamp(pageable));
        return responseHelper.ok(MessageCode.SUCCESS, data);
    }

    @GetMapping("/{jobId}/download")
    @Operation(summary = "Download exported file presigned URL", description = "Generate MinIO presigned URL to download the report file directly")
    public ResponseEntity<BaseResponse<Map<String, String>>> downloadFile(@PathVariable String jobId) {
        String downloadUrl = jobService.downloadFile(jobId);
        Map<String, String> data = Map.of("downloadUrl", downloadUrl);
        return responseHelper.ok(MessageCode.SUCCESS, data);
    }

    @PostMapping("/{jobId}/cancel")
    @Operation(summary = "Cancel export job", description = "Cancel a pending or processing export job")
    public ResponseEntity<BaseResponse<Void>> cancelJob(@PathVariable String jobId) {
        jobService.cancelJob(jobId);
        return responseHelper.ok(MessageCode.SUCCESS, HttpStatus.OK);
    }
}
