package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.ChangePasswordRequest;
import com.ledger.ledgerservice.model.dto.request.UpdateMyProfileRequest;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.UserProfileResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.user.UserProfileService;
import com.ledger.ledgerservice.util.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;
    private final ResponseHelper responseHelper;

    @GetMapping
    public ResponseEntity<BaseResponse<UserProfileResponse>> getMyProfile() {
        UserProfileResponse data = userProfileService.getMyProfile();
        return responseHelper.ok(MessageCode.USER_PROFILE_FETCHED, data, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<BaseResponse<UserProfileResponse>> updateMyProfile(@Valid @RequestBody UpdateMyProfileRequest request) {
        UserProfileResponse data = userProfileService.updateMyProfile(request);
        return responseHelper.ok(MessageCode.USER_PROFILE_UPDATED, data, HttpStatus.OK);
    }

    @PutMapping("/password")
    public ResponseEntity<BaseResponse<Object>> changeMyPassword(@Valid @RequestBody ChangePasswordRequest request) {
        userProfileService.changeMyPassword(request);
        return responseHelper.ok(MessageCode.PASSWORD_CHANGED, null, HttpStatus.OK);
    }
}
