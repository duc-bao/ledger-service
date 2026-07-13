package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.ChangePasswordRequest;
import com.ledger.ledgerservice.model.dto.request.UpdateMyProfileRequest;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.UserProfileResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.user.UserProfileService;
import com.ledger.ledgerservice.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "APIs for the current user's profile")
@SecurityRequirement(name = "bearerAuth")
public class UserProfileController {
    private final UserProfileService userProfileService;
    private final ResponseHelper responseHelper;

    @GetMapping
    @Operation(summary = "Get my profile")
    public ResponseEntity<BaseResponse<UserProfileResponse>> getMyProfile() {
        UserProfileResponse data = userProfileService.getMyProfile();
        return responseHelper.ok(MessageCode.USER_PROFILE_FETCHED, data, HttpStatus.OK);
    }

    @PutMapping
    @Operation(summary = "Update my profile")
    public ResponseEntity<BaseResponse<UserProfileResponse>> updateMyProfile(@Valid @RequestBody UpdateMyProfileRequest request) {
        UserProfileResponse data = userProfileService.updateMyProfile(request);
        return responseHelper.ok(MessageCode.USER_PROFILE_UPDATED, data, HttpStatus.OK);
    }

    @PutMapping("/password")
    @Operation(summary = "Change my password")
    public ResponseEntity<BaseResponse<Object>> changeMyPassword(@Valid @RequestBody ChangePasswordRequest request) {
        userProfileService.changeMyPassword(request);
        return responseHelper.ok(MessageCode.PASSWORD_CHANGED, null, HttpStatus.OK);
    }
}
