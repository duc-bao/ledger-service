package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.CreateRoleRequest;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.RoleResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.security.RoleAdminService;
import com.ledger.ledgerservice.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/rbac/roles")
@RequiredArgsConstructor
@Tag(name = "RBAC Role Admin", description = "APIs for role administration")
@SecurityRequirement(name = "bearerAuth")
public class RoleAdminController {
    private final RoleAdminService roleAdminService;
    private final ResponseHelper responseHelper;

    @PostMapping
    @Operation(summary = "Create role")
    public ResponseEntity<BaseResponse<RoleResponse>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return responseHelper.ok(MessageCode.SUCCESS, roleAdminService.createRole(request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "Delete role")
    public ResponseEntity<BaseResponse<Object>> deleteRole(@PathVariable String roleId) {
        roleAdminService.deleteRole(roleId);
        return responseHelper.ok(MessageCode.SUCCESS, HttpStatus.OK);
    }
}
