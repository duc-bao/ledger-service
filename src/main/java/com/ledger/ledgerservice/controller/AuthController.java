package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.request.LoginRequestOtpRequest;
import com.ledger.ledgerservice.model.dto.request.LoginVerifyOtpRequest;
import com.ledger.ledgerservice.model.dto.request.ForgotPasswordRequestOtpRequest;
import com.ledger.ledgerservice.model.dto.request.ForgotPasswordVerifyOtpRequest;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.LoginTokenResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.auth.AuthService;
import com.ledger.ledgerservice.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "APIs for OTP-based login authentication and logout")
public class AuthController {
    private final AuthService authService;
    private final ResponseHelper responseHelper;

    @PostMapping("/request-otp")
    @Operation(summary = "Request login OTP", description = "Send an OTP to a valid email/account to start the login flow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<BaseResponse<LoginTokenResponse>> requestOtp(@Valid @RequestBody LoginRequestOtpRequest request) {
        LoginTokenResponse data = authService.requestLoginOtp(request);
        MessageCode code = data.isTwoFactorRequired() ? MessageCode.OTP_SENT : MessageCode.SUCCESS;
        return responseHelper.ok(code, data, HttpStatus.OK);
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Verify the OTP and return an access token to call authenticated APIs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP verified successfully"),
            @ApiResponse(responseCode = "400", description = "OTP is invalid or expired")
    })
    public ResponseEntity<BaseResponse<LoginTokenResponse>> verifyOtp(@Valid @RequestBody LoginVerifyOtpRequest request) {
        LoginTokenResponse data = authService.verifyLoginOtp(request);
        return responseHelper.ok(MessageCode.SUCCESS, data, HttpStatus.OK);
    }

    @PostMapping("/forgot-password/request-otp")
    @Operation(summary = "Request forgot password OTP", description = "Verify email and account status, then send OTP to the email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<BaseResponse<Object>> requestForgotPasswordOtp(
            @Valid @RequestBody ForgotPasswordRequestOtpRequest request) {
        authService.requestForgotPasswordOtp(request);
        return responseHelper.ok(MessageCode.OTP_SENT, null, HttpStatus.OK);
    }

    @PostMapping("/forgot-password/verify-otp")
    @Operation(summary = "Verify forgot password OTP", description = "Verify OTP sent to email for forgot password flow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP verified successfully"),
            @ApiResponse(responseCode = "400", description = "OTP is invalid or expired")
    })
    public ResponseEntity<BaseResponse<Object>> verifyForgotPasswordOtp(
            @Valid @RequestBody ForgotPasswordVerifyOtpRequest request) {
        authService.verifyForgotPasswordOtp(request);
        return responseHelper.ok(MessageCode.SUCCESS, null, HttpStatus.OK);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoke the current session by adding the token to the blacklist", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged out successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or invalid token")
    })
    public ResponseEntity<BaseResponse<Object>> logout(HttpServletRequest request) {
        authService.logout(request);
        return responseHelper.ok(MessageCode.LOGOUT_SUCCESS, null, HttpStatus.OK);
    }
}
