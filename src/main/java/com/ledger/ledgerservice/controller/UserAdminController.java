package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.AdminUserSearchRequest;
import com.ledger.ledgerservice.model.dto.response.AdminUserDetailResponse;
import com.ledger.ledgerservice.model.dto.response.AdminUserItemResponse;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.MetaDataResp;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.user.UserAdminService;
import com.ledger.ledgerservice.util.ResponseHelper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserAdminController {
    private final UserAdminService userAdminService;
    private final ResponseHelper responseHelper;

    @PostMapping("/search")
    public ResponseEntity<BaseResponse<List<AdminUserItemResponse>>> getUsers(@Valid @RequestBody AdminUserSearchRequest request) {
        Page<AdminUserItemResponse> page = userAdminService.getUsers(request);

        MetaDataResp metaData = MetaDataResp.builder()
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .build();
        return responseHelper.ok(MessageCode.SUCCESS, page.getContent(), metaData, HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<BaseResponse<AdminUserDetailResponse>> getUserDetail(@PathVariable String userId) {
        AdminUserDetailResponse data = userAdminService.getUserDetail(userId);
        return responseHelper.ok(MessageCode.SUCCESS, data, HttpStatus.OK);
    }

    @PutMapping("/{userId}/lock")
    public ResponseEntity<BaseResponse<Object>> lockUser(@PathVariable String userId) {
        userAdminService.lockUser(userId);
        return responseHelper.ok(MessageCode.SUCCESS, null, HttpStatus.OK);
    }

    @PutMapping("/{userId}/unlock")
    public ResponseEntity<BaseResponse<Object>> unlockUser(@PathVariable String userId) {
        userAdminService.unlockUser(userId);
        return responseHelper.ok(MessageCode.SUCCESS, null, HttpStatus.OK);
    }
}
