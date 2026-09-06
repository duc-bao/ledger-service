package com.ledger.ledgerservice.service.auth;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class OtpCacheService {
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration LOCK_TTL = Duration.ofMinutes(5);
    private static final Duration RESEND_TTL = Duration.ofMinutes(1);
    private static final Duration RESEND_WINDOW_TTL = Duration.ofMinutes(5);
    private static final Duration SPAM_LOCK_TTL = Duration.ofMinutes(15);
    private static final Duration IP_RATE_LIMIT_TTL = Duration.ofMinutes(5);
    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_RESEND_PER_WINDOW = 3;
    private static final int SPAM_RESEND_THRESHOLD = 5;
    private static final int MAX_IP_REQUESTS = 10;

    private final RedissonClient redissonClient;

    public OtpCacheService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void putOtp(String username, String otpCode) {
        redissonClient.<String>getBucket(getOtpKey(username)).set(otpCode, OTP_TTL);
        redissonClient.getAtomicLong(getAttemptKey(username)).delete();
        redissonClient.getBucket(getLockKey(username)).delete();
        redissonClient.getBucket(getResendKey(username)).set(Boolean.TRUE, RESEND_TTL);
    }

    public boolean isResendCoolingDown(String username) {
        return redissonClient.getBucket(getResendKey(username)).isExists();
    }

    public ResendCheckResult checkAndRecordResend(String target) {
        String norm = normalize(target);
        if (isSpamLocked(norm)) {
            return ResendCheckResult.SPAM_BLOCKED;
        }

        RAtomicLong counter = redissonClient.getAtomicLong(getResendCountKey(norm));
        if (!counter.isExists()) {
            counter.set(0);
            counter.expire(RESEND_WINDOW_TTL);
        }

        long current = counter.incrementAndGet();
        if (current > SPAM_RESEND_THRESHOLD) {
            redissonClient.getBucket(getSpamLockKey(norm)).set(Boolean.TRUE, SPAM_LOCK_TTL);
            return ResendCheckResult.SPAM_BLOCKED;
        }
        if (current > MAX_RESEND_PER_WINDOW) {
            return ResendCheckResult.LIMIT_EXCEEDED;
        }
        return ResendCheckResult.ALLOWED;
    }

    public boolean isSpamLocked(String target) {
        return redissonClient.getBucket(getSpamLockKey(normalize(target))).isExists();
    }

    public boolean checkIpRateLimit(String ip, String action) {
        if (!StringUtils.hasText(ip)) {
            return true;
        }
        String key = "auth:ratelimit:ip:" + action + ":" + ip.trim();
        RAtomicLong counter = redissonClient.getAtomicLong(key);
        if (!counter.isExists()) {
            counter.set(0);
            counter.expire(IP_RATE_LIMIT_TTL);
        }
        long current = counter.incrementAndGet();
        return current <= MAX_IP_REQUESTS;
    }

    public boolean isLocked(String username) {
        return redissonClient.getBucket(getLockKey(username)).isExists();
    }

    public boolean hasOtp(String username) {
        return redissonClient.getBucket(getOtpKey(username)).isExists();
    }

    public String getOtp(String username) {
        return redissonClient.<String>getBucket(getOtpKey(username)).get();
    }

    public AuthAttemptState recordFailedAttempt(String username) {
        RAtomicLong counter = redissonClient.getAtomicLong(getAttemptKey(username));
        if (!counter.isExists()) {
            counter.set(0);
            counter.expire(OTP_TTL);
        }
        long current = counter.incrementAndGet();
        int remaining = Math.max(MAX_ATTEMPTS - (int) current, 0);
        if (current >= MAX_ATTEMPTS) {
            RBucket<Boolean> lockBucket = redissonClient.getBucket(getLockKey(username));
            lockBucket.set(Boolean.TRUE, LOCK_TTL);
            clearOtp(username);
            return AuthAttemptState.locked(remaining, MAX_ATTEMPTS, LOCK_TTL.toSeconds());
        }
        return AuthAttemptState.failed(remaining, MAX_ATTEMPTS);
    }

    public void clearOtp(String username) {
        redissonClient.getBucket(getOtpKey(username)).delete();
        redissonClient.getAtomicLong(getAttemptKey(username)).delete();
    }

    public void clearFailures(String username) {
        redissonClient.getAtomicLong(getAttemptKey(username)).delete();
        redissonClient.getBucket(getLockKey(username)).delete();
    }

    public long getRemainingAttempts(String username) {
        RAtomicLong counter = redissonClient.getAtomicLong(getAttemptKey(username));
        long used = counter.isExists() ? counter.get() : 0;
        long remaining = MAX_ATTEMPTS - used;
        return Math.max(remaining, 0);
    }

    public long getLockRemainingSeconds(String username) {
        long ttl = redissonClient.getBucket(getLockKey(username)).remainTimeToLive();
        if (ttl <= 0) {
            return 0;
        }
        return TimeUnit.MILLISECONDS.toSeconds(ttl);
    }

    private String getOtpKey(String username) {
        return "auth:otp:" + normalize(username);
    }

    private String getAttemptKey(String username) {
        return "auth:otp:attempt:" + normalize(username);
    }

    private String getLockKey(String username) {
        return "auth:otp:lock:" + normalize(username);
    }

    private String getResendKey(String username) {
        return "auth:otp:resend:" + normalize(username);
    }

    private String getResendCountKey(String target) {
        return "auth:otp:resend_count:" + normalize(target);
    }

    private String getSpamLockKey(String target) {
        return "auth:otp:spam_lock:" + normalize(target);
    }

    private String normalize(String username) {
        return StringUtils.hasText(username) ? username.trim().toLowerCase() : "";
    }

    public enum ResendCheckResult {
        ALLOWED,
        LIMIT_EXCEEDED,
        SPAM_BLOCKED
    }
}
