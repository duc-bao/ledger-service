package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.PermissionApiCreateRequest;
import com.ledger.ledgerservice.model.dto.request.PermissionApiSearchRequest;
import com.ledger.ledgerservice.model.dto.request.PermissionApiUpdateRequest;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.PageResponse;
import com.ledger.ledgerservice.model.dto.response.PermissionApiResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.security.PermissionApiService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/rbac/permission-apis")
@RequiredArgsConstructor
@Tag(name = "RBAC Permission API", description = "Manage API to permission mappings")
@SecurityRequirement(name = "bearerAuth")
public class PermissionApiController {
    private final PermissionApiService permissionApiService;
    private final ResponseHelper responseHelper;

    @PostMapping
    @Operation(summary = "Create permission API mapping", description = "Create one mapping in idp_permission_apis")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mapping created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Permission not found"),
            @ApiResponse(responseCode = "409", description = "Mapping already exists")
    })
    public ResponseEntity<BaseResponse<PermissionApiResponse>> create(
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Permission API creation payload")
            PermissionApiCreateRequest request
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, permissionApiService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{permissionApiId}")
    @Operation(summary = "Update permission API mapping", description = "Update one mapping in idp_permission_apis")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mapping updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Permission API or permission not found"),
            @ApiResponse(responseCode = "409", description = "Mapping already exists")
    })
    public ResponseEntity<BaseResponse<PermissionApiResponse>> update(
            @Parameter(description = "Permission API identifier", required = true)
            @PathVariable String permissionApiId,
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Permission API update payload")
            PermissionApiUpdateRequest request
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, permissionApiService.update(permissionApiId, request));
    }

    @GetMapping("/{permissionApiId}")
    @Operation(summary = "Get permission API mapping", description = "Fetch permission API mapping detail by identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mapping found"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Permission API not found")
    })
    public ResponseEntity<BaseResponse<PermissionApiResponse>> getById(
            @Parameter(description = "Permission API identifier", required = true)
            @PathVariable String permissionApiId
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, permissionApiService.getById(permissionApiId));
    }

    @GetMapping
    @Operation(summary = "Search permission API mappings", description = "Search active or inactive API mappings with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search completed"),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<BaseResponse<PageResponse<PermissionApiResponse>>> search(
            @ParameterObject PermissionApiSearchRequest request,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, permissionApiService.search(request, PageableUtil.clamp(pageable)));
    }

    @DeleteMapping("/{permissionApiId}")
    @Operation(summary = "Delete permission API mapping", description = "Delete one API mapping by identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mapping deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Permission API not found")
    })
    public ResponseEntity<BaseResponse<Object>> delete(
            @Parameter(description = "Permission API identifier", required = true)
            @PathVariable String permissionApiId
    ) {
        permissionApiService.delete(permissionApiId);
        return responseHelper.ok(MessageCode.SUCCESS, HttpStatus.OK);
    }
}
