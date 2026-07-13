package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.response.AuthorizedMenuListResponse;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.security.AccessControlQueryService;
import com.ledger.ledgerservice.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/access-control")
@RequiredArgsConstructor
@Tag(name = "Access Control Query", description = "APIs for querying permissions and menus of the current user")
public class AccessControlQueryController {

    private final AccessControlQueryService accessControlQueryService;
    private final ResponseHelper responseHelper;

    @GetMapping("/me/menus")
    @Operation(summary = "Get authorized menus", description = "Return menus that the current user is authorized to access", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authorized menu list retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<BaseResponse<AuthorizedMenuListResponse>> getMyAuthorizedMenus() {
        AuthorizedMenuListResponse response = accessControlQueryService.getAuthorizedMenus();
        return responseHelper.ok(MessageCode.SUCCESS, response);
    }
}
