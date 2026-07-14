package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.RolePermissionCreateRequest;
import com.ledger.ledgerservice.model.dto.request.RolePermissionReplaceRequest;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.PermissionResponse;
import com.ledger.ledgerservice.model.dto.response.RolePermissionResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.security.RolePermissionService;
import com.ledger.ledgerservice.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/rbac/role-permissions")
@RequiredArgsConstructor
@Tag(name = "RBAC Role Permission", description = "Manage role to permission assignments")
@SecurityRequirement(name = "bearerAuth")
public class RolePermissionController {
    private final RolePermissionService rolePermissionService;
    private final ResponseHelper responseHelper;

    @PostMapping
    @Operation(summary = "Assign permission to role", description = "Create one role-permission mapping in idp_role_permissions")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mapping created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Role or permission not found"),
            @ApiResponse(responseCode = "409", description = "Mapping already exists")
    })
    public ResponseEntity<BaseResponse<RolePermissionResponse>> assign(
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Role-permission assignment payload")
            RolePermissionCreateRequest request
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, rolePermissionService.assign(request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{rolePermissionId}")
    @Operation(summary = "Revoke role permission", description = "Delete one role-permission mapping by identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mapping deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Mapping not found")
    })
    public ResponseEntity<BaseResponse<Object>> revoke(
            @Parameter(description = "Role-permission mapping identifier", required = true)
            @PathVariable String rolePermissionId
    ) {
        rolePermissionService.revoke(rolePermissionId);
        return responseHelper.ok(MessageCode.SUCCESS, HttpStatus.OK);
    }

    @GetMapping("/roles/{roleId}/permissions")
    @Operation(summary = "Get permissions by role", description = "Return all active permissions granted to a role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissions loaded"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<BaseResponse<List<PermissionResponse>>> getPermissionsByRole(
            @Parameter(description = "Role identifier", required = true)
            @PathVariable String roleId
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, rolePermissionService.getPermissionsByRole(roleId));
    }

    @PutMapping("/roles/{roleId}/permissions")
    @Operation(summary = "Replace role permissions", description = "Diff and replace active permission assignments for a role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role permissions replaced"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Role or permission not found")
    })
    public ResponseEntity<BaseResponse<Object>> replaceRolePermissions(
            @Parameter(description = "Role identifier", required = true)
            @PathVariable String roleId,
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Target permission identifiers for the role")
            RolePermissionReplaceRequest request
    ) {
        rolePermissionService.replaceRolePermissions(roleId, request);
        return responseHelper.ok(MessageCode.SUCCESS, HttpStatus.OK);
    }
}
