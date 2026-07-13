package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.AdminUserSearchRequest;
import com.ledger.ledgerservice.model.dto.response.*;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.user.UserAdminService;
import com.ledger.ledgerservice.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "User Admin", description = "APIs for user account administration")
@SecurityRequirement(name = "bearerAuth")
public class UserAdminController {
    private final UserAdminService userAdminService;
    private final ResponseHelper responseHelper;

    @PostMapping("/search")
    @Operation(summary = "Search users")
    public ResponseEntity<BaseResponse<List<AdminUserItemResponse>>> getUsers(@Valid @RequestBody AdminUserSearchRequest request) {
        Page<AdminUserItemResponse> page = userAdminService.getUsers(request);
        MetaDataResp metaData = MetaDataResp.builder().totalElements(page.getTotalElements()).totalPages(page.getTotalPages()).page(page.getNumber() + 1).size(page.getSize()).build();
        return responseHelper.ok(MessageCode.SUCCESS, page.getContent(), metaData, HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user details")
    public ResponseEntity<BaseResponse<AdminUserDetailResponse>> getUserDetail(@PathVariable String userId) {
        AdminUserDetailResponse data = userAdminService.getUserDetail(userId);
        return responseHelper.ok(MessageCode.SUCCESS, data, HttpStatus.OK);
    }

    @PutMapping("/{userId}/lock")
    @Operation(summary = "Lock user account")
    public ResponseEntity<BaseResponse<Object>> lockUser(@PathVariable String userId) {
        userAdminService.lockUser(userId);
        return responseHelper.ok(MessageCode.SUCCESS, null, HttpStatus.OK);
    }

    @PutMapping("/{userId}/unlock")
    @Operation(summary = "Unlock user account")
    public ResponseEntity<BaseResponse<Object>> unlockUser(@PathVariable String userId) {
        userAdminService.unlockUser(userId);
        return responseHelper.ok(MessageCode.SUCCESS, null, HttpStatus.OK);
    }
}
