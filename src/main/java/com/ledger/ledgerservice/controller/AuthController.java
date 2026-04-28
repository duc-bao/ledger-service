package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.LoginRequestOtpRequest;
import com.ledger.ledgerservice.model.dto.request.LoginVerifyOtpRequest;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.LoginTokenResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.auth.AuthService;
import com.ledger.ledgerservice.util.ResponseHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/login")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final ResponseHelper responseHelper;

    @PostMapping("/request-otp")
    public ResponseEntity<BaseResponse<Object>> requestOtp(@Valid @RequestBody LoginRequestOtpRequest request) {
        authService.requestLoginOtp(request);
        return responseHelper.ok(MessageCode.OTP_SENT, null, HttpStatus.OK);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<BaseResponse<LoginTokenResponse>> verifyOtp(@Valid @RequestBody LoginVerifyOtpRequest request) {
        LoginTokenResponse data = authService.verifyLoginOtp(request);
        return responseHelper.ok(MessageCode.SUCCESS, data, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Object>> logout(HttpServletRequest request) {
        authService.logout(request);
        return responseHelper.ok(MessageCode.LOGOUT_SUCCESS, null, HttpStatus.OK);
    }
}
