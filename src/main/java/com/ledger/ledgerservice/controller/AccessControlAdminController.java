package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.AdminCreateUserRequest;
import com.ledger.ledgerservice.model.dto.request.AssignUserRoleRequest;
import com.ledger.ledgerservice.model.dto.response.AdminCreateUserResponse;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/access-control")
@RequiredArgsConstructor
@Tag(name = "Access Control Admin", description = "Compatibility admin APIs for global user-role assignment")
@SecurityRequirement(name = "bearerAuth")
public class AccessControlAdminController {
    private final AccessControlAdminService accessControlAdminService;
    private final ResponseHelper responseHelper;

    @PostMapping("/users/assign-role")
    @Operation(summary = "Assign global role to user")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Role assigned successfully"))
    public ResponseEntity<BaseResponse<UserRoleResponse>> assignRole(@Valid @RequestBody AssignUserRoleRequest request) {
        return responseHelper.ok(MessageCode.SUCCESS, accessControlAdminService.assignRoleForUser(request));
    }

    @PostMapping("/users")
    @Operation(summary = "Create user by admin")
    public ResponseEntity<BaseResponse<AdminCreateUserResponse>> createUserByAdmin(@Valid @RequestBody AdminCreateUserRequest request) {
        return responseHelper.ok(MessageCode.SUCCESS, accessControlAdminService.createUserByAdmin(request));
    }
}