package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.MenuPermissionCreateRequest;
import com.ledger.ledgerservice.model.dto.response.AuthorizedMenuResponse;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.MenuPermissionResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.security.MenuPermissionService;
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
@RequestMapping("/api/v1/admin/rbac/menu-permissions")
@RequiredArgsConstructor
@Tag(name = "RBAC Menu Permission", description = "Manage UI menu to permission mappings")
@SecurityRequirement(name = "bearerAuth")
public class MenuPermissionController {
    private final MenuPermissionService menuPermissionService;
    private final ResponseHelper responseHelper;

    @PostMapping
    @Operation(summary = "Assign menu permission", description = "Bind one RBAC permission to one UI menu for frontend rendering")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mapping created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Menu or permission not found"),
            @ApiResponse(responseCode = "409", description = "Mapping already exists")
    })
    public ResponseEntity<BaseResponse<MenuPermissionResponse>> assign(
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Menu-permission assignment payload")
            MenuPermissionCreateRequest request
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, menuPermissionService.assign(request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{menuPermissionId}")
    @Operation(summary = "Revoke menu permission", description = "Delete one menu-permission mapping")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mapping deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Mapping not found")
    })
    public ResponseEntity<BaseResponse<Object>> revoke(
            @Parameter(description = "Menu permission identifier", required = true)
            @PathVariable String menuPermissionId
    ) {
        menuPermissionService.revoke(menuPermissionId);
        return responseHelper.ok(MessageCode.SUCCESS, HttpStatus.OK);
    }

    @GetMapping("/menus/{menuId}")
    @Operation(summary = "Get menu permissions", description = "Return active RBAC permission mappings of one menu")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mappings loaded"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Menu not found")
    })
    public ResponseEntity<BaseResponse<List<MenuPermissionResponse>>> getByMenu(
            @Parameter(description = "Menu identifier", required = true)
            @PathVariable String menuId
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, menuPermissionService.getByMenu(menuId));
    }

    @GetMapping("/users/{userId}/authorized-menus")
    @Operation(summary = "Get authorized menus", description = "Resolve frontend menu tree for a user from RBAC permissions only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authorized menus loaded"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<BaseResponse<List<AuthorizedMenuResponse>>> getAuthorizedMenus(
            @Parameter(description = "User identifier", required = true)
            @PathVariable String userId
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, menuPermissionService.getAuthorizedMenus(userId));
    }
}
