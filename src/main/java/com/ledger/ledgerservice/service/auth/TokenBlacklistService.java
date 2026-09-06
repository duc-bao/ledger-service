package com.ledger.ledgerservice.service.auth;

import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

@Service
public class TokenBlacklistService {
    private static final String BLACKLIST_JTI_PREFIX = "auth:token:blacklist:jti:";
    private static final String BLACKLIST_HASH_PREFIX = "auth:token:blacklist:hash:";

    private final RedissonClient redissonClient;

    public TokenBlacklistService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void blacklistJti(String jti, Duration ttl) {
        if (!StringUtils.hasText(jti) || ttl == null || ttl.isNegative() || ttl.isZero()) {
            return;
        }
        redissonClient.getBucket(BLACKLIST_JTI_PREFIX + jti.trim()).set(Boolean.TRUE, ttl);
    }

    public boolean isJtiBlacklisted(String jti) {
        if (!StringUtils.hasText(jti)) {
            return false;
        }
        return redissonClient.getBucket(BLACKLIST_JTI_PREFIX + jti.trim()).isExists();
    }

    public void blacklist(String tokenOrJti, Duration ttl) {
        if (!StringUtils.hasText(tokenOrJti) || ttl == null || ttl.isNegative() || ttl.isZero()) {
            return;
        }
        redissonClient.getBucket(resolveKey(tokenOrJti)).set(Boolean.TRUE, ttl);
    }

    public boolean isBlacklisted(String tokenOrJti) {
        if (!StringUtils.hasText(tokenOrJti)) {
            return false;
        }
        return redissonClient.getBucket(resolveKey(tokenOrJti)).isExists();
    }

    private String resolveKey(String tokenOrJti) {
        String clean = tokenOrJti.trim();
        if (clean.length() <= 64 && !clean.contains(".")) {
            return BLACKLIST_JTI_PREFIX + clean;
        }
        return BLACKLIST_HASH_PREFIX + sha256Hex(clean);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(input.hashCode());
        }
    }
}
