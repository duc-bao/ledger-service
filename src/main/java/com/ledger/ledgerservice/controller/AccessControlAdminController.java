package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.AdminCreateUserRequest;
import com.ledger.ledgerservice.model.dto.request.AssignUserRoleRequest;
import com.ledger.ledgerservice.model.dto.request.CreateRoleWithPermissionsRequest;
import com.ledger.ledgerservice.model.dto.request.UpsertGroupPermissionsRequest;
import com.ledger.ledgerservice.model.dto.response.AdminCreateUserResponse;
import com.ledger.ledgerservice.model.dto.response.AdminPermissionDetailResponse;
import com.ledger.ledgerservice.model.dto.response.AdminPermissionListResponse;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.EffectivePermissionResponse;
import com.ledger.ledgerservice.model.dto.response.RoleWithPermissionsResponse;
import com.ledger.ledgerservice.model.dto.response.UserRoleResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.security.AccessControlAdminService;
import com.ledger.ledgerservice.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/access-control")
@RequiredArgsConstructor
@Tag(name = "Access Control Admin", description = "Admin APIs for users, roles and permissions")
@SecurityRequirement(name = "bearerAuth")
public class AccessControlAdminController {

    private final AccessControlAdminService accessControlAdminService;
    private final ResponseHelper responseHelper;

    @PostMapping("/users/assign-role")
    @Operation(summary = "Assign role to user")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Role assigned successfully"))
    public ResponseEntity<BaseResponse<UserRoleResponse>> assignRole(@Valid @RequestBody AssignUserRoleRequest request) {
        UserRoleResponse response = accessControlAdminService.assignRoleForUser(request);
        return responseHelper.ok(MessageCode.SUCCESS, response);
    }

    @PostMapping("/users")
    @Operation(summary = "Create user by admin")
    public ResponseEntity<BaseResponse<AdminCreateUserResponse>> createUserByAdmin(@Valid @RequestBody AdminCreateUserRequest request) {
        AdminCreateUserResponse response = accessControlAdminService.createUserByAdmin(request);
        return responseHelper.ok(MessageCode.SUCCESS, response);
    }

    @PostMapping("/roles")
    @Operation(summary = "Create role with permissions")
    public ResponseEntity<BaseResponse<RoleWithPermissionsResponse>> createRoleWithPermissions(@Valid @RequestBody CreateRoleWithPermissionsRequest request) {
        RoleWithPermissionsResponse response = accessControlAdminService.createRoleWithPermissions(request);
        return responseHelper.ok(MessageCode.SUCCESS, response);
    }

    @PostMapping("/groups/permissions")
    @Operation(summary = "Upsert group permissions")
    public ResponseEntity<BaseResponse<Object>> upsertGroupPermissions(@Valid @RequestBody UpsertGroupPermissionsRequest request) {
        accessControlAdminService.upsertGroupPermissions(request);
        return responseHelper.ok(MessageCode.SUCCESS, HttpStatus.OK);
    }

    @GetMapping("/users/{userId}/effective-permissions")
    @Operation(summary = "Get user effective permissions")
    public ResponseEntity<BaseResponse<EffectivePermissionResponse>> getEffectivePermissions(@PathVariable String userId) {
        EffectivePermissionResponse response = accessControlAdminService.getEffectivePermissions(userId);
        return responseHelper.ok(MessageCode.SUCCESS, response);
    }

    @GetMapping("/permissions")
    @Operation(summary = "Get permission list")
    public ResponseEntity<BaseResponse<AdminPermissionListResponse>> getPermissionList() {
        AdminPermissionListResponse response = accessControlAdminService.getPermissionList();
        return responseHelper.ok(MessageCode.SUCCESS, response);
    }

    @GetMapping("/groups/{groupId}/permissions")
    @Operation(summary = "Get group permission details")
    public ResponseEntity<BaseResponse<AdminPermissionDetailResponse>> getGroupPermissionDetail(@PathVariable String groupId) {
        AdminPermissionDetailResponse response = accessControlAdminService.getGroupPermissionDetail(groupId);
        return responseHelper.ok(MessageCode.SUCCESS, response);
    }
}
