package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.AuditLogSearchRequest;
import com.ledger.ledgerservice.model.dto.response.*;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.audit.ActionLogService;
import com.ledger.ledgerservice.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "APIs for searching and retrieving audit logs")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogAdminController {
    private final ActionLogService actionLogService;
    private final ResponseHelper responseHelper;

    @PostMapping("/search")
    @Operation(summary = "Search audit logs")
    public ResponseEntity<BaseResponse<List<AuditLogItemResponse>>> search(@Valid @RequestBody AuditLogSearchRequest request) {
        Page<AuditLogItemResponse> page = actionLogService.search(request);
        MetaDataResp metaData = MetaDataResp.builder().totalElements(page.getTotalElements()).totalPages(page.getTotalPages()).page(page.getNumber() + 1).size(page.getSize()).build();
        return responseHelper.ok(MessageCode.SUCCESS, page.getContent(), metaData, org.springframework.http.HttpStatus.OK);
    }

    @GetMapping("/{logId}")
    @Operation(summary = "Get audit log details")
    public ResponseEntity<BaseResponse<AuditLogResponse>> detail(@PathVariable String logId) {
        AuditLogResponse data = actionLogService.getDetail(logId);
        return responseHelper.ok(MessageCode.SUCCESS, data);
    }
}
