package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.MenuCreateRequest;
import com.ledger.ledgerservice.model.dto.request.MenuSearchRequest;
import com.ledger.ledgerservice.model.dto.request.MenuStatusRequest;
import com.ledger.ledgerservice.model.dto.request.MenuUpdateRequest;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.MenuResponse;
import com.ledger.ledgerservice.model.dto.response.PageResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.service.menu.MenuService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/rbac/menus")
@RequiredArgsConstructor
@Tag(name = "RBAC Menu", description = "Manage frontend menus and their RBAC bindings")
@SecurityRequirement(name = "bearerAuth")
public class MenuController {
    private final MenuService menuService;
    private final ResponseHelper responseHelper;

    @PostMapping
    @Operation(summary = "Create menu", description = "Create a new menu and optionally bind RBAC permissions")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Menu created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Parent menu or permission not found"),
            @ApiResponse(responseCode = "409", description = "Menu code already exists")
    })
    public ResponseEntity<BaseResponse<MenuResponse>> create(
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Menu creation payload")
            MenuCreateRequest request
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, menuService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{menuId}")
    @Operation(summary = "Update menu", description = "Update an existing menu and optionally replace RBAC permission bindings")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Menu or permission not found")
    })
    public ResponseEntity<BaseResponse<MenuResponse>> update(
            @Parameter(description = "Menu identifier", required = true)
            @PathVariable String menuId,
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Menu update payload")
            MenuUpdateRequest request
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, menuService.update(menuId, request));
    }

    @GetMapping("/{menuId}")
    @Operation(summary = "Get menu by id", description = "Fetch menu detail by identifier")
    public ResponseEntity<BaseResponse<MenuResponse>> getById(
            @Parameter(description = "Menu identifier", required = true)
            @PathVariable String menuId
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, menuService.getById(menuId));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get menu by code", description = "Fetch menu detail by business code")
    public ResponseEntity<BaseResponse<MenuResponse>> getByCode(
            @Parameter(description = "Menu code", required = true, example = "USER_MANAGEMENT")
            @PathVariable String code
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, menuService.getByCode(code));
    }

    @GetMapping
    @Operation(summary = "Search menus", description = "Search menus with pagination")
    public ResponseEntity<BaseResponse<PageResponse<MenuResponse>>> search(
            @ParameterObject MenuSearchRequest request,
            @ParameterObject Pageable pageable
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, menuService.search(request, pageable));
    }

    @GetMapping("/tree")
    @Operation(summary = "Get menu tree", description = "Load menus ordered as a tree with optional filters")
    public ResponseEntity<BaseResponse<List<MenuResponse>>> getTree(
            @RequestParam(required = false) RecordStatus status,
            @RequestParam(required = false) Boolean visibleOnly
    ) {
        return responseHelper.ok(MessageCode.SUCCESS, menuService.getTree(status, visibleOnly));
    }

    @PatchMapping("/{menuId}/status")
    @Operation(summary = "Change menu status", description = "Activate or deactivate a menu")
    public ResponseEntity<BaseResponse<Object>> changeStatus(
            @Parameter(description = "Menu identifier", required = true)
            @PathVariable String menuId,
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Target status payload")
            MenuStatusRequest request
    ) {
        menuService.changeStatus(menuId, request.getStatus());
        return responseHelper.ok(MessageCode.SUCCESS, HttpStatus.OK);
    }

    @DeleteMapping("/{menuId}")
    @Operation(summary = "Delete menu", description = "Delete a menu after validating child menus and RBAC bindings")
    public ResponseEntity<BaseResponse<Object>> delete(
            @Parameter(description = "Menu identifier", required = true)
            @PathVariable String menuId
    ) {
        menuService.delete(menuId);
        return responseHelper.ok(MessageCode.SUCCESS, HttpStatus.OK);
    }
}
