package com.ledger.ledgerservice.service.auth;

import com.ledger.ledgerservice.config.properties.JwtProperties;
import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.request.LoginRequestOtpRequest;
import com.ledger.ledgerservice.model.dto.request.LoginVerifyOtpRequest;
import com.ledger.ledgerservice.model.dto.response.LoginTokenResponse;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.UserStatus;
import com.ledger.ledgerservice.repository.UserRepository;
import com.ledger.ledgerservice.service.email.EmailTemplateService;
import com.ledger.ledgerservice.util.JwtUtils;
import com.ledger.ledgerservice.util.MessageHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private OtpCacheService otpCacheService;
    @Mock
    private EmailTemplateService emailTemplateService;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private MessageHelper messageHelper;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private AuthAttemptCacheService authAttemptCacheService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("12345678901234567890123456789012");
        jwtProperties.setTimeToLive(3600);
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                otpCacheService,
                emailTemplateService,
                jwtUtils,
                jwtProperties,
                messageHelper,
                tokenBlacklistService,
                authAttemptCacheService
        );
    }

    @Test
    void requestLoginOtpWhenTwoFactorDisabledReturnsTokenAndUpdatesLastLoginAt() {
        LoginRequestOtpRequest request = new LoginRequestOtpRequest();
        request.setUsername("user1");
        request.setPassword("Password123");
        User user = activeUser();
        user.setTwoFactorEnabled(false);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", user.getPassword())).thenReturn(true);
        when(jwtUtils.generateToken(anyMap(), anyString(), anyInt(), eq(TimeUnit.SECONDS))).thenReturn("token");

        LoginTokenResponse response = authService.requestLoginOtp(request);

        assertFalse(response.isTwoFactorRequired());
        assertEquals("token", response.getAccessToken());
        assertNotNull(user.getLastLoginAt());
        verify(authAttemptCacheService).clearPasswordFailures("user1");
        verify(emailTemplateService, never()).sendAsync(any());
        verify(userRepository).save(user);
    }

    @Test
    void requestLoginOtpWhenPasswordInvalidRecordsRemainingAttempts() {
        LoginRequestOtpRequest request = new LoginRequestOtpRequest();
        request.setUsername("user1");
        request.setPassword("bad");
        User user = activeUser();
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", user.getPassword())).thenReturn(false);
        when(authAttemptCacheService.recordPasswordFailure("user1"))
                .thenReturn(AuthAttemptState.failed(4, 5));

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.requestLoginOtp(request));

        assertEquals(MessageCode.PASSWORD_INVALID.getCode(), exception.getCode());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertNotNull(exception.getData());
    }

    @Test
    void requestLoginOtpWhenPasswordFailuresExhaustedLocksForFiveMinutes() {
        LoginRequestOtpRequest request = new LoginRequestOtpRequest();
        request.setUsername("user1");
        request.setPassword("bad");
        User user = activeUser();
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", user.getPassword())).thenReturn(false);
        when(authAttemptCacheService.recordPasswordFailure("user1"))
                .thenReturn(AuthAttemptState.locked(0, 5, 300));

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.requestLoginOtp(request));

        assertEquals(MessageCode.PASSWORD_ATTEMPTS_EXCEEDED.getCode(), exception.getCode());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatusCode());
    }

    @Test
    void verifyLoginOtpUpdatesLastLoginAtAndClearsOtpFailures() {
        LoginVerifyOtpRequest request = new LoginVerifyOtpRequest();
        request.setUsername("user1");
        request.setOtp("123456");
        User user = activeUser();
        user.setTwoFactorEnabled(true);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(otpCacheService.getOtp("user1")).thenReturn("123456");
        when(jwtUtils.generateToken(anyMap(), anyString(), anyInt(), eq(TimeUnit.SECONDS))).thenReturn("token");

        LoginTokenResponse response = authService.verifyLoginOtp(request);

        assertEquals("token", response.getAccessToken());
        assertNotNull(user.getLastLoginAt());
        verify(otpCacheService).clearOtp("user1");
        verify(otpCacheService).clearFailures("user1");
        verify(userRepository).save(user);
    }

    @Test
    void verifyLoginOtpWhenOtpInvalidIncludesRemainingAttempts() {
        LoginVerifyOtpRequest request = new LoginVerifyOtpRequest();
        request.setUsername("user1");
        request.setOtp("000000");
        User user = activeUser();
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(otpCacheService.getOtp("user1")).thenReturn("123456");
        when(otpCacheService.recordFailedAttempt("user1")).thenReturn(AuthAttemptState.failed(4, 5));

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.verifyLoginOtp(request));

        assertEquals(MessageCode.OTP_INVALID.getCode(), exception.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertNotNull(exception.getData());
    }

    @Test
    void resendLoginOtpWhenValidGeneratesNewOtpAndSendsEmail() {
        com.ledger.ledgerservice.model.dto.request.LoginResendOtpRequest request =
                new com.ledger.ledgerservice.model.dto.request.LoginResendOtpRequest("user1");
        User user = activeUser();
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(otpCacheService.hasOtp("user1")).thenReturn(true);
        when(otpCacheService.isSpamLocked("user1")).thenReturn(false);
        when(otpCacheService.isResendCoolingDown("user1")).thenReturn(false);
        when(otpCacheService.checkAndRecordResend("user1")).thenReturn(OtpCacheService.ResendCheckResult.ALLOWED);

        authService.resendLoginOtp(request);

        verify(otpCacheService).putOtp(eq("user1"), anyString());
        verify(emailTemplateService).sendAsync(any());
    }

    @Test
    void resendLoginOtpWhenNoPriorOtpThrowsSessionNotFound() {
        com.ledger.ledgerservice.model.dto.request.LoginResendOtpRequest request =
                new com.ledger.ledgerservice.model.dto.request.LoginResendOtpRequest("user1");
        User user = activeUser();
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(otpCacheService.hasOtp("user1")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.resendLoginOtp(request));
        assertEquals(MessageCode.OTP_SESSION_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void resendLoginOtpWhenTwoFactorDisabledThrowsBadRequest() {
        com.ledger.ledgerservice.model.dto.request.LoginResendOtpRequest request =
                new com.ledger.ledgerservice.model.dto.request.LoginResendOtpRequest("user1");
        User user = activeUser();
        user.setTwoFactorEnabled(false);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.resendLoginOtp(request));
        assertEquals(MessageCode.INPUT_INVALID.getCode(), exception.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void resendLoginOtpWhenLimitExceededThrowsTooManyRequests() {
        com.ledger.ledgerservice.model.dto.request.LoginResendOtpRequest request =
                new com.ledger.ledgerservice.model.dto.request.LoginResendOtpRequest("user1");
        User user = activeUser();
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(otpCacheService.hasOtp("user1")).thenReturn(true);
        when(otpCacheService.isSpamLocked("user1")).thenReturn(false);
        when(otpCacheService.isResendCoolingDown("user1")).thenReturn(false);
        when(otpCacheService.checkAndRecordResend("user1")).thenReturn(OtpCacheService.ResendCheckResult.LIMIT_EXCEEDED);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.resendLoginOtp(request));
        assertEquals(MessageCode.OTP_RESEND_LIMIT_EXCEEDED.getCode(), exception.getCode());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatusCode());
    }

    @Test
    void resendLoginOtpWhenSpamBlockedThrowsSpamBlocked() {
        com.ledger.ledgerservice.model.dto.request.LoginResendOtpRequest request =
                new com.ledger.ledgerservice.model.dto.request.LoginResendOtpRequest("user1");
        User user = activeUser();
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(otpCacheService.hasOtp("user1")).thenReturn(true);
        when(otpCacheService.isSpamLocked("user1")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.resendLoginOtp(request));
        assertEquals(MessageCode.OTP_RESEND_SPAM_BLOCKED.getCode(), exception.getCode());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatusCode());
    }

    private User activeUser() {
        return User.builder()
                .id("user-id")
                .username("user1")
                .password("encoded")
                .email("user1@example.com")
                .status(UserStatus.ACTIVE)
                .build();
    }
}
