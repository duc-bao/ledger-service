package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.CreateDepartmentRequest;
import com.ledger.ledgerservice.model.dto.request.UpdateDepartmentRequest;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.DepartmentResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.department.DepartmentAdminService;
import com.ledger.ledgerservice.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
