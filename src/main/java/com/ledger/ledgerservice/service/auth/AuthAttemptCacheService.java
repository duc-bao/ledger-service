package com.ledger.ledgerservice.service.auth;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class AuthAttemptCacheService {
    private static final Duration ATTEMPT_TTL = Duration.ofMinutes(5);
    private static final Duration LOCK_TTL = Duration.ofMinutes(5);
    private static final int MAX_ATTEMPTS = 5;

    private final RedissonClient redissonClient;

    public AuthAttemptCacheService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public boolean isPasswordLocked(String username) {
        return redissonClient.getBucket(getLockKey(username)).isExists();
    }

    public AuthAttemptState getPasswordLockState(String username) {
        return AuthAttemptState.locked(0, MAX_ATTEMPTS, getPasswordLockRemainingSeconds(username));
    }

    public AuthAttemptState recordPasswordFailure(String username) {
        RAtomicLong counter = redissonClient.getAtomicLong(getAttemptKey(username));
        if (!counter.isExists()) {
            counter.set(0);
            counter.expire(ATTEMPT_TTL);
        }
        long current = counter.incrementAndGet();
        int remaining = Math.max(MAX_ATTEMPTS - (int) current, 0);
        if (current >= MAX_ATTEMPTS) {
            redissonClient.getBucket(getLockKey(username)).set(Boolean.TRUE, LOCK_TTL);
            counter.delete();
            return AuthAttemptState.locked(remaining, MAX_ATTEMPTS, LOCK_TTL.toSeconds());
        }
        return AuthAttemptState.failed(remaining, MAX_ATTEMPTS);
    }

    public void clearPasswordFailures(String username) {
        redissonClient.getAtomicLong(getAttemptKey(username)).delete();
        redissonClient.getBucket(getLockKey(username)).delete();
    }

    public long getPasswordLockRemainingSeconds(String username) {
        long ttl = redissonClient.getBucket(getLockKey(username)).remainTimeToLive();
        return ttl <= 0 ? 0 : TimeUnit.MILLISECONDS.toSeconds(ttl);
    }

    private String getAttemptKey(String username) {
        return "auth:password:attempt:" + normalize(username);
    }

    private String getLockKey(String username) {
        return "auth:password:lock:" + normalize(username);
    }

    private String normalize(String username) {
        return StringUtils.hasText(username) ? username.trim().toLowerCase() : "";
    }
}
