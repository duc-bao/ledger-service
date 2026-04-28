package com.ledger.ledgerservice.service.auth;

import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Service
public class TokenBlacklistService {
    private static final String BLACKLIST_PREFIX = "auth:token:blacklist:";

    private final RedissonClient redissonClient;

    public TokenBlacklistService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void blacklist(String token, Duration ttl) {
        if (!StringUtils.hasText(token) || ttl == null || ttl.isNegative() || ttl.isZero()) {
            return;
        }
        redissonClient.getBucket(getKey(token)).set(Boolean.TRUE, ttl);
    }

    public boolean isBlacklisted(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        return redissonClient.getBucket(getKey(token)).isExists();
    }

    private String getKey(String token) {
        return BLACKLIST_PREFIX + token;
    }
}
