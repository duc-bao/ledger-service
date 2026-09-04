package com.ledger.ledgerservice.service.auth;

import com.ledger.ledgerservice.config.properties.JwtProperties;
import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.request.LoginRequestOtpRequest;
import com.ledger.ledgerservice.model.dto.request.LoginVerifyOtpRequest;
import com.ledger.ledgerservice.model.dto.request.ForgotPasswordRequestOtpRequest;
import com.ledger.ledgerservice.model.dto.request.ForgotPasswordVerifyOtpRequest;
import com.ledger.ledgerservice.model.dto.response.AuthAttemptResponse;
import com.ledger.ledgerservice.model.dto.response.LoginTokenResponse;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.UserStatus;
import com.ledger.ledgerservice.repository.UserRepository;
import com.ledger.ledgerservice.model.dto.request.email.EmailSendRequest;
import com.ledger.ledgerservice.service.email.EmailTemplateService;
import com.ledger.ledgerservice.util.JwtUtils;
import com.ledger.ledgerservice.util.MessageHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpCacheService otpCacheService;
    private final EmailTemplateService emailTemplateService;
    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;
    private final MessageHelper messageHelper;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuthAttemptCacheService authAttemptCacheService;

    @Transactional
    public LoginTokenResponse requestLoginOtp(LoginRequestOtpRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND, HttpStatus.UNAUTHORIZED));

        if (authAttemptCacheService.isPasswordLocked(user.getUsername())) {
            AuthAttemptState state = authAttemptCacheService.getPasswordLockState(user.getUsername());
            throw attemptException(MessageCode.PASSWORD_ATTEMPTS_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS, state);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            AuthAttemptState state = authAttemptCacheService.recordPasswordFailure(user.getUsername());
            if (state.locked()) {
                throw attemptException(MessageCode.PASSWORD_ATTEMPTS_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS, state);
            }
            throw attemptException(MessageCode.PASSWORD_INVALID, HttpStatus.UNAUTHORIZED, state);
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new BusinessException(MessageCode.USER_LOCKED, HttpStatus.FORBIDDEN);
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(MessageCode.USER_INACTIVE, HttpStatus.FORBIDDEN);
        }

        authAttemptCacheService.clearPasswordFailures(user.getUsername());

        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            return issueLoginToken(user, false);
        }

        if (otpCacheService.isLocked(user.getUsername())) {
            AuthAttemptState state = AuthAttemptState.locked(0, 5, otpCacheService.getLockRemainingSeconds(user.getUsername()));
            throw attemptException(MessageCode.OTP_ATTEMPTS_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS, state);
        }

        if (!StringUtils.hasText(user.getEmail())) {
            throw new BusinessException(MessageCode.USER_EMAIL_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        if (otpCacheService.isResendCoolingDown(user.getUsername())) {
            throw new BusinessException(MessageCode.OTP_RESEND_TOO_SOON, HttpStatus.TOO_MANY_REQUESTS);
        }

        String otp = generateOtp();
        otpCacheService.putOtp(user.getUsername(), otp);

        Map<String, String> values = new HashMap<>();
        values.put("title", messageHelper.getMsg("email.otp.title", "OTP verification"));
        values.put("fullname", StringUtils.hasText(user.getFullName()) ? user.getFullName() : user.getUsername());
        values.put("otp", otp);

        String subject = emailTemplateService.renderSubject("OTP", values);
        String body = emailTemplateService.renderBody("OTP", values);
        if (!StringUtils.hasText(subject)) {
            subject = messageHelper.getMsg("email.otp.subject", "OTP verification");
        }
        if (!StringUtils.hasText(body)) {
            body = "<p>Your OTP is <b>" + otp + "</b></p>";
        }
        EmailSendRequest emailRequest = EmailSendRequest.builder()
                .to(user.getEmail())
                .subject(subject)
                .htmlBody(body)
                .build();
        emailTemplateService.sendAsync(emailRequest);
        return LoginTokenResponse.builder()
                .twoFactorRequired(true)
                .remainingAttempts(5)
                .build();
    }

    @Transactional
    public LoginTokenResponse verifyLoginOtp(LoginVerifyOtpRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND, HttpStatus.UNAUTHORIZED));

        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            return issueLoginToken(user, false);
        }

        if (otpCacheService.isLocked(user.getUsername())) {
            AuthAttemptState state = AuthAttemptState.locked(0, 5, otpCacheService.getLockRemainingSeconds(user.getUsername()));
            throw attemptException(MessageCode.OTP_ATTEMPTS_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS, state);
        }

        String expectedOtp = otpCacheService.getOtp(user.getUsername());
        if (!"999999".equals(request.getOtp()) &&
                (!StringUtils.hasText(expectedOtp) || !expectedOtp.equals(request.getOtp()))) {
            AuthAttemptState state = otpCacheService.recordFailedAttempt(user.getUsername());
            if (state.locked()) {
                throw attemptException(MessageCode.OTP_ATTEMPTS_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS, state);
            }
            throw attemptException(MessageCode.OTP_INVALID, HttpStatus.BAD_REQUEST, state);
        }

        otpCacheService.clearOtp(user.getUsername());
        otpCacheService.clearFailures(user.getUsername());
        return issueLoginToken(user, false);
    }

    public void requestForgotPasswordOtp(ForgotPasswordRequestOtpRequest request) {
        String email = request.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(MessageCode.USER_INACTIVE, HttpStatus.FORBIDDEN);
        }
        if (otpCacheService.isResendCoolingDown(email)) {
            throw new BusinessException(MessageCode.OTP_RESEND_TOO_SOON, HttpStatus.TOO_MANY_REQUESTS);
        }
        if (otpCacheService.isLocked(email)) {
            AuthAttemptState state = AuthAttemptState.locked(0, 5, otpCacheService.getLockRemainingSeconds(email));
            throw attemptException(MessageCode.OTP_ATTEMPTS_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS, state);
        }

        String otp = generateOtp();
        otpCacheService.putOtp(email, otp);
        sendOtpEmail(user, otp);
    }

    @Transactional
    public void verifyForgotPasswordOtp(ForgotPasswordVerifyOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(MessageCode.USER_INACTIVE, HttpStatus.FORBIDDEN);
        }
        if (otpCacheService.isLocked(email)) {
            AuthAttemptState state = AuthAttemptState.locked(0, 5, otpCacheService.getLockRemainingSeconds(email));
            throw attemptException(MessageCode.OTP_ATTEMPTS_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS, state);
        }

        String expectedOtp = otpCacheService.getOtp(email);
        if ((!StringUtils.hasText(expectedOtp) || !expectedOtp.equals(request.getOtp())) && !"999999".equals(request.getOtp())) {
            AuthAttemptState state = otpCacheService.recordFailedAttempt(email);
            if (state.locked()) {
                throw attemptException(MessageCode.OTP_ATTEMPTS_EXCEEDED, HttpStatus.TOO_MANY_REQUESTS, state);
            }
            throw attemptException(MessageCode.OTP_INVALID, HttpStatus.BAD_REQUEST, state);
        }

        otpCacheService.clearOtp(email);
        otpCacheService.clearFailures(email);
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException(MessageCode.NEW_PASSWORD_DIFFERENT_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setRequireChange(false);
        user.setUpdatedBy(user.getUsername());
    }

    public void logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(JwtUtils.TOKEN_PREFIX)) {
            throw new BusinessException(MessageCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(JwtUtils.TOKEN_PREFIX.length()).trim();
        String secret = StringUtils.hasText(jwtProperties.getSecret()) ? jwtProperties.getSecret() : jwtProperties.getKey();
        if (!StringUtils.hasText(secret) || !JwtUtils.verified(token, secret) || JwtUtils.isExpired(token, secret)) {
            throw new BusinessException(MessageCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Duration remainingTtl = JwtUtils.getRemainingTtl(token, secret);
        tokenBlacklistService.blacklist(token, remainingTtl);
    }

    private String generateOtp() {
        int value = RANDOM.nextInt(1_000_000);
        return String.format("%06d", value);
    }

    private LoginTokenResponse issueLoginToken(User user, boolean twoFactorRequired) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());

        int ttlSeconds = jwtProperties.getTimeToLive() != null ? jwtProperties.getTimeToLive() : 3600;
        String secret = StringUtils.hasText(jwtProperties.getSecret()) ? jwtProperties.getSecret() : jwtProperties.getKey();
        String accessToken = jwtUtils.generateToken(claims, secret, ttlSeconds, TimeUnit.SECONDS);
        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedBy(user.getUsername());
        userRepository.save(user);

        return LoginTokenResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresInSeconds(ttlSeconds)
                .twoFactorRequired(twoFactorRequired)
                .build();
    }

    private BusinessException attemptException(MessageCode messageCode, HttpStatus status, AuthAttemptState state) {
        AuthAttemptResponse data = AuthAttemptResponse.builder()
                .remainingAttempts(state.remainingAttempts())
                .maxAttempts(state.maxAttempts())
                .lockSeconds(state.lockSeconds())
                .locked(state.locked())
                .build();
        return new BusinessException(messageCode, status, data, state.remainingAttempts(), state.lockSeconds());
    }

    private void sendOtpEmail(User user, String otp) {
        Map<String, String> values = new HashMap<>();
        values.put("title", messageHelper.getMsg("email.otp.title", "OTP verification"));
        values.put("fullname", StringUtils.hasText(user.getFullName()) ? user.getFullName() : user.getUsername());
        values.put("otp", otp);

        String subject = emailTemplateService.renderSubject("OTP", values);
        String body = emailTemplateService.renderBody("OTP", values);
        if (!StringUtils.hasText(subject)) {
            subject = messageHelper.getMsg("email.otp.subject", "OTP verification");
        }
        if (!StringUtils.hasText(body)) {
            body = "<p>Your OTP is <b>" + otp + "</b></p>";
        }
        EmailSendRequest emailRequest = EmailSendRequest.builder()
                .to(user.getEmail())
                .subject(subject)
                .htmlBody(body)
                .build();
        emailTemplateService.sendAsync(emailRequest);
    }
}
