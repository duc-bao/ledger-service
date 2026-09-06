package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.PermissionCreateRequest;
import com.ledger.ledgerservice.model.dto.request.PermissionSearchRequest;
import com.ledger.ledgerservice.model.dto.request.PermissionStatusRequest;
import com.ledger.ledgerservice.model.dto.request.PermissionUpdateRequest;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.PageResponse;
import com.ledger.ledgerservice.model.dto.response.PermissionResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.security.PermissionService;
import com.ledger.ledgerservice.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import com.ledger.ledgerservice.util.PageableUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/rbac/permissions")
@RequiredArgsConstructor
@Tag(name = "RBAC Permission", description = "Manage RBAC permission definitions")
@SecurityRequirement(name = "bearerAuth")
public class PermissionController {
    private final PermissionService permissionService;
    private final ResponseHelper responseHelper;

    @PostMapping
    @Operation(summary = "Create permission", description = "Create a new permission definition in idp_permission_definitions")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Permission created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Permission code already exists")
    })
    public ResponseEntity<BaseResponse<PermissionResponse>> create(
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Permission creation payload")
            PermissionCreateRequest request
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, permissionService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{permissionId}")
    @Operation(summary = "Update permission", description = "Update an existing permission definition")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permission updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Permission not found"),
            @ApiResponse(responseCode = "409", description = "Permission code already exists")
    })
    public ResponseEntity<BaseResponse<PermissionResponse>> update(
            @Parameter(description = "Permission identifier", required = true)
            @PathVariable String permissionId,
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Permission update payload")
            PermissionUpdateRequest request
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, permissionService.update(permissionId, request));
    }

    @GetMapping("/{permissionId}")
    @Operation(summary = "Get permission by id", description = "Fetch permission definition detail by identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permission found"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Permission not found")
    })
    public ResponseEntity<BaseResponse<PermissionResponse>> getById(
            @Parameter(description = "Permission identifier", required = true)
            @PathVariable String permissionId
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, permissionService.getById(permissionId));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get permission by code", description = "Fetch permission definition detail by business code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permission found"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Permission not found")
    })
    public ResponseEntity<BaseResponse<PermissionResponse>> getByCode(
            @Parameter(description = "Permission business code", required = true, example = "USER_CREATE")
            @PathVariable String code
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, permissionService.getByCode(code));
    }

    @GetMapping
    @Operation(summary = "Search permissions", description = "Search permission definitions with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search completed"),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<BaseResponse<PageResponse<PermissionResponse>>> search(
            @ParameterObject PermissionSearchRequest request,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, permissionService.search(request, PageableUtil.clamp(pageable)));
    }

    @PatchMapping("/{permissionId}/status")
    @Operation(summary = "Change permission status", description = "Activate or deactivate a permission definition")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status changed"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Permission not found")
    })
    public ResponseEntity<BaseResponse<Object>> changeStatus(
            @Parameter(description = "Permission identifier", required = true)
            @PathVariable String permissionId,
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Target status payload")
            PermissionStatusRequest request
    ) {
        permissionService.changeStatus(permissionId, request.getStatus());
        return responseHelper.ok(MessageCode.SUCCESS, HttpStatus.OK);
    }

    @DeleteMapping("/{permissionId}")
    @Operation(summary = "Delete permission", description = "Delete a permission definition when it is not referenced by RBAC mappings")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permission deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Permission not found"),
            @ApiResponse(responseCode = "409", description = "Permission is in use")
    })
    public ResponseEntity<BaseResponse<Object>> delete(
            @Parameter(description = "Permission identifier", required = true)
            @PathVariable String permissionId
    ) {
        permissionService.delete(permissionId);
        return responseHelper.ok(MessageCode.SUCCESS, HttpStatus.OK);
    }
}
