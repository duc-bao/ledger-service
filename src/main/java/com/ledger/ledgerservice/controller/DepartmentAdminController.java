package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.CreateDepartmentRequest;
import com.ledger.ledgerservice.model.dto.request.DepartmentSearchRequest;
import com.ledger.ledgerservice.model.dto.request.UpdateDepartmentRequest;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.DepartmentResponse;
import com.ledger.ledgerservice.model.dto.response.DepartmentTreeResponse;
import com.ledger.ledgerservice.model.dto.response.MetaDataResp;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.department.DepartmentAdminService;
import com.ledger.ledgerservice.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/departments")
@RequiredArgsConstructor
@Tag(name = "Department Admin", description = "APIs for department administration")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentAdminController {
    private final DepartmentAdminService departmentAdminService;
    private final ResponseHelper responseHelper;

    @PostMapping
    @Operation(summary = "Create department")
    public ResponseEntity<BaseResponse<DepartmentResponse>> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        return responseHelper.ok(MessageCode.SUCCESS, departmentAdminService.createDepartment(request), HttpStatus.CREATED);
    }

    @PutMapping("/{departmentId}")
    @Operation(summary = "Update department")
    public ResponseEntity<BaseResponse<DepartmentResponse>> updateDepartment(@PathVariable String departmentId,
                                                                             @Valid @RequestBody UpdateDepartmentRequest request) {
        return responseHelper.ok(MessageCode.SUCCESS, departmentAdminService.updateDepartment(departmentId, request), HttpStatus.OK);
    }

    @GetMapping("/tree")
    @Operation(summary = "Get department tree")
    public ResponseEntity<BaseResponse<List<DepartmentTreeResponse>>> getDepartmentTree() {
        return responseHelper.ok(MessageCode.SUCCESS, departmentAdminService.getDepartmentTree(), HttpStatus.OK);
    }

    @GetMapping("/{departmentId}")
    @Operation(summary = "Get department")
    public ResponseEntity<BaseResponse<DepartmentResponse>> getDepartment(@PathVariable String departmentId) {
        return responseHelper.ok(MessageCode.SUCCESS, departmentAdminService.getDepartment(departmentId), HttpStatus.OK);
    }

    @PostMapping("/search")
    @Operation(summary = "Search departments")
    public ResponseEntity<BaseResponse<List<DepartmentResponse>>> searchDepartments(@RequestBody(required = false) DepartmentSearchRequest request) {
        Page<DepartmentResponse> page = departmentAdminService.searchDepartments(request);
        MetaDataResp metaData = MetaDataResp.builder()
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .build();
        return responseHelper.ok(MessageCode.SUCCESS, page.getContent(), metaData, HttpStatus.OK);
    }
}
