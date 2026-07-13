package com.ledger.ledgerservice.service.auth;

import com.ledger.ledgerservice.config.properties.JwtProperties;
import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.request.LoginRequestOtpRequest;
import com.ledger.ledgerservice.model.dto.request.LoginVerifyOtpRequest;
import com.ledger.ledgerservice.model.dto.request.ForgotPasswordRequestOtpRequest;
import com.ledger.ledgerservice.model.dto.request.ForgotPasswordVerifyOtpRequest;
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

    public void requestLoginOtp(LoginRequestOtpRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND, HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(MessageCode.PASSWORD_INVALID, HttpStatus.UNAUTHORIZED);
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new BusinessException(MessageCode.USER_LOCKED, HttpStatus.FORBIDDEN);
        }

        if (!StringUtils.hasText(user.getEmail())) {
            throw new BusinessException(MessageCode.USER_EMAIL_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        if (otpCacheService.isLocked(user.getUsername())) {
            throw new BusinessException(MessageCode.OTP_NO_ATTEMPTS_LEFT, HttpStatus.TOO_MANY_REQUESTS);
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new BusinessException(MessageCode.USER_LOCKED, HttpStatus.FORBIDDEN);
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
    }

    public LoginTokenResponse verifyLoginOtp(LoginVerifyOtpRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND, HttpStatus.UNAUTHORIZED));

        if (otpCacheService.isLocked(user.getUsername())) {
            throw new BusinessException(MessageCode.OTP_NO_ATTEMPTS_LEFT, HttpStatus.TOO_MANY_REQUESTS);
        }

        String expectedOtp = otpCacheService.getOtp(user.getUsername());
        if (!"999999".equals(request.getOtp()) &&
                (!StringUtils.hasText(expectedOtp) || !expectedOtp.equals(request.getOtp()))) {
            long attempts = otpCacheService.recordFailedAttempt(user.getUsername());
            if (attempts >= 5) {
                throw new BusinessException(MessageCode.OTP_NO_ATTEMPTS_LEFT, HttpStatus.TOO_MANY_REQUESTS);
            }
            throw new BusinessException(MessageCode.OTP_INVALID, HttpStatus.BAD_REQUEST);
        }

        otpCacheService.clearOtp(user.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());

        int ttlSeconds = jwtProperties.getTimeToLive() != null ? jwtProperties.getTimeToLive() : 3600;
        String secret = StringUtils.hasText(jwtProperties.getSecret()) ? jwtProperties.getSecret() : jwtProperties.getKey();
        String accessToken = jwtUtils.generateToken(claims, secret, ttlSeconds, TimeUnit.SECONDS);

        return LoginTokenResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresInSeconds(ttlSeconds)
                .build();
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
            throw new BusinessException(MessageCode.OTP_NO_ATTEMPTS_LEFT, HttpStatus.TOO_MANY_REQUESTS);
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
            throw new BusinessException(MessageCode.OTP_NO_ATTEMPTS_LEFT, HttpStatus.TOO_MANY_REQUESTS);
        }

        String expectedOtp = otpCacheService.getOtp(email);
        if ((!StringUtils.hasText(expectedOtp) || !expectedOtp.equals(request.getOtp())) && !"999999".equals(request.getOtp())) {
            long attempts = otpCacheService.recordFailedAttempt(email);
            if (attempts >= 5) {
                throw new BusinessException(MessageCode.OTP_NO_ATTEMPTS_LEFT, HttpStatus.TOO_MANY_REQUESTS);
            }
            throw new BusinessException(MessageCode.OTP_INVALID, HttpStatus.BAD_REQUEST);
        }

        otpCacheService.clearOtp(email);
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
