package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.company.CompanyUserRoleCreateRequest;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.PermissionResponse;
import com.ledger.ledgerservice.model.dto.response.company.CompanyUserRoleResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.company.CompanyUserRoleService;
import com.ledger.ledgerservice.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/companies/user-roles")
@RequiredArgsConstructor
@Tag(name = "Company User Role", description = "APIs for company scoped user-role assignments")
@SecurityRequirement(name = "bearerAuth")
public class CompanyUserRoleController {
    private final CompanyUserRoleService companyUserRoleService;
    private final ResponseHelper responseHelper;

    @PostMapping
    @Operation(summary = "Assign company scoped role")
    public ResponseEntity<BaseResponse<CompanyUserRoleResponse>> assign(@Valid @RequestBody CompanyUserRoleCreateRequest request) {
        return responseHelper.ok(MessageCode.SUCCESS, companyUserRoleService.assign(request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{companyUserRoleId}")
    @Operation(summary = "Revoke company scoped role")
    public ResponseEntity<BaseResponse<Object>> revoke(@PathVariable String companyUserRoleId) {
        companyUserRoleService.revoke(companyUserRoleId);
        return responseHelper.ok(MessageCode.SUCCESS, HttpStatus.OK);
    }

    @GetMapping("/companies/{companyId}/users/{userId}")
    @Operation(summary = "Get company roles of user")
    public ResponseEntity<BaseResponse<List<CompanyUserRoleResponse>>> getByCompanyAndUser(@PathVariable String companyId,
                                                                                           @PathVariable String userId) {
        return responseHelper.ok(MessageCode.SUCCESS, companyUserRoleService.getByCompanyAndUser(companyId, userId), HttpStatus.OK);
    }

    @GetMapping("/companies/{companyId}/users/{userId}/permissions")
    @Operation(summary = "Get company scoped permissions of user")
    public ResponseEntity<BaseResponse<List<PermissionResponse>>> getPermissions(@PathVariable String companyId,
                                                                                 @PathVariable String userId) {
        return responseHelper.ok(MessageCode.SUCCESS, companyUserRoleService.getPermissions(companyId, userId), HttpStatus.OK);
    }
}
