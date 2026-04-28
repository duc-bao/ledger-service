package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.response.AuthorizedMenuListResponse;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.security.AccessControlQueryService;
import com.ledger.ledgerservice.util.ResponseHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/access-control")
@RequiredArgsConstructor
public class AccessControlQueryController {

    private final AccessControlQueryService accessControlQueryService;
    private final ResponseHelper responseHelper;

    @GetMapping("/me/menus")
    public ResponseEntity<BaseResponse<AuthorizedMenuListResponse>> getMyAuthorizedMenus() {
        AuthorizedMenuListResponse response = accessControlQueryService.getAuthorizedMenus();
        return responseHelper.ok(MessageCode.SUCCESS, response);
    }
}
