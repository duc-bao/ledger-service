package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.AuditLogSearchRequest;
import com.ledger.ledgerservice.model.dto.response.AuditLogItemResponse;
import com.ledger.ledgerservice.model.dto.response.AuditLogResponse;
import com.ledger.ledgerservice.model.dto.response.AuditLogSearchResponse;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.MetaDataResp;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.audit.ActionLogService;
import com.ledger.ledgerservice.util.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
public class AuditLogAdminController {
    private final ActionLogService actionLogService;
    private final ResponseHelper responseHelper;

    @PostMapping("/search")
    public ResponseEntity<BaseResponse<List<AuditLogItemResponse>>> search(@Valid @RequestBody AuditLogSearchRequest request) {
        Page<AuditLogItemResponse> page = actionLogService.search(request);
        MetaDataResp metaData = MetaDataResp.builder()
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .build();
        return responseHelper.ok(MessageCode.SUCCESS, page.getContent(), metaData, org.springframework.http.HttpStatus.OK);
    }

    @GetMapping("/{logId}")
    public ResponseEntity<BaseResponse<AuditLogResponse>> detail(@PathVariable String logId) {
        AuditLogResponse data = actionLogService.getDetail(logId);
        return responseHelper.ok(MessageCode.SUCCESS, data);
    }
}
