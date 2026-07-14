package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.DepartmentUserRoleCreateRequest;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.DepartmentUserRoleResponse;
import com.ledger.ledgerservice.model.dto.response.PermissionResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.security.DepartmentUserRoleService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/rbac/department-user-roles")
@RequiredArgsConstructor
@Tag(name = "RBAC Department User Role", description = "Manage department scoped role assignments")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentUserRoleController {
    private final DepartmentUserRoleService departmentUserRoleService;
    private final ResponseHelper responseHelper;

    @PostMapping
    @Operation(summary = "Assign department role", description = "Assign an RBAC role to one active department membership")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mapping created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Membership or role not found"),
            @ApiResponse(responseCode = "409", description = "Mapping already exists")
    })
    public ResponseEntity<BaseResponse<DepartmentUserRoleResponse>> assign(
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Department role assignment payload")
            DepartmentUserRoleCreateRequest request
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, departmentUserRoleService.assign(request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{departmentUserRoleId}")
    @Operation(summary = "Revoke department role", description = "Delete one department membership role mapping")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mapping deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Mapping not found")
    })
    public ResponseEntity<BaseResponse<Object>> revoke(
            @Parameter(description = "Department user role identifier", required = true)
            @PathVariable String departmentUserRoleId
    ) {
        departmentUserRoleService.revoke(departmentUserRoleId);
        return responseHelper.ok(MessageCode.SUCCESS, HttpStatus.OK);
    }

    @GetMapping("/users/{userId}/departments/{departmentId}")
    @Operation(summary = "Get department roles of user", description = "Return active department role assignments of a user in one department")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mappings loaded"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Department membership not found")
    })
    public ResponseEntity<BaseResponse<List<DepartmentUserRoleResponse>>> getByUserAndDepartment(
            @Parameter(description = "User identifier", required = true)
            @PathVariable String userId,
            @Parameter(description = "Department identifier", required = true)
            @PathVariable String departmentId
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, departmentUserRoleService.getByUserAndDepartment(userId, departmentId));
    }

    @GetMapping("/users/{userId}/departments/{departmentId}/permissions")
    @Operation(summary = "Get department permissions of user", description = "Resolve active permissions of a user within one department from department roles only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissions loaded"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Department membership not found")
    })
    public ResponseEntity<BaseResponse<List<PermissionResponse>>> getPermissionsByUserAndDepartment(
            @Parameter(description = "User identifier", required = true)
            @PathVariable String userId,
            @Parameter(description = "Department identifier", required = true)
            @PathVariable String departmentId
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, departmentUserRoleService.getPermissionsByUserAndDepartment(userId, departmentId));
    }
}
