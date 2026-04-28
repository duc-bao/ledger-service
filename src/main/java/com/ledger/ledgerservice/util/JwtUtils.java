package com.ledger.ledgerservice.util;

import com.ledger.ledgerservice.model.constant.CommonConstant;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JwtUtils {
    public static final String TOKEN_PREFIX = "Bearer ";

    public String generateToken(Map<String, Object> values, String secret, int timeActive, TimeUnit unitTime) {
        String token = null;
        try {
            JWSSigner signer = new MACSigner(generateShareSecret(secret));
            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
            Date date = new Date(System.currentTimeMillis() + unitTime.toMillis(timeActive));
            builder.expirationTime(date);
            List<String> myList = new ArrayList<>(values.keySet());
            for (String key : myList) {
                builder.claim(key, values.get(key));
            }

            JWTClaimsSet claimsSet = builder.build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);
            token = signedJWT.serialize();
        } catch (Exception e) {
            log.error("generateToken error = {}", e.getMessage(), e);
        }
        return token;
    }



    private byte[] generateShareSecret(String secret) {
        byte[] key = secret.getBytes(StandardCharsets.UTF_8);
        if (key.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters for HS256");
        }
        return key;
    }

    @SneakyThrows
    public static boolean verified(String token, String secret) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secret);
            return signedJWT.verify(verifier);
        } catch (Exception ex) {
            log.error("Error verified with Jwt failed={}", ex.getMessage(), ex);
            return false;
        }
    }

    public static boolean isExpired(String token, String secret) {
        try {
            if (!StringUtils.hasText(token) || !StringUtils.hasText(secret)) {
                return true;
            }

            JWTClaimsSet claimsSet = getJWTClaimsSet(token, secret);
            if (Objects.isNull(claimsSet))
                return true;

            return isExpired(claimsSet.getExpirationTime());
        } catch (Exception ex) {
            log.error("Error isExpired = {}", ex.getMessage(), ex);
            return true;
        }
    }

    public static Duration getRemainingTtl(String token, String secret) {
        try {
            if (!StringUtils.hasText(token) || !StringUtils.hasText(secret)) {
                return Duration.ZERO;
            }
            JWTClaimsSet claimsSet = getJWTClaimsSet(token, secret);
            if (claimsSet == null || claimsSet.getExpirationTime() == null) {
                return Duration.ZERO;
            }
            long remainingMillis = claimsSet.getExpirationTime().getTime() - System.currentTimeMillis();
            if (remainingMillis <= 0) {
                return Duration.ZERO;
            }
            return Duration.ofMillis(remainingMillis);
        } catch (Exception ex) {
            log.error("Error getRemainingTtl = {}", ex.getMessage(), ex);
            return Duration.ZERO;
        }
    }

    @SneakyThrows
    public static Map<String, Object> getClaims(String token, String secret) {
        try {
            JWTClaimsSet claimsSet = getJWTClaimsSet(token, secret);
            if (Objects.isNull(claimsSet))
                return new HashMap<>();

            if (isExpired(claimsSet.getExpirationTime()))
                return new HashMap<>();

            return claimsSet.getClaims();
        } catch (Exception ex) {
            log.error("Error getClaims = {}", ex.getMessage(), ex);
        }

        return new HashMap<>();
    }

    private static boolean isExpired(Date expirationTime) {
        return expirationTime.before(new Date());
    }

    private static JWTClaimsSet getJWTClaimsSet(String token, String secret) {
        JWTClaimsSet claimsSet = null;
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier jwsVerifier = new MACVerifier(secret);
            if (signedJWT.verify(jwsVerifier)) {
                claimsSet = signedJWT.getJWTClaimsSet();
            }

        } catch (Exception ex) {
            log.error("Error get JWT claims set -{}", ex.getMessage(), ex);
        }
        return claimsSet;

    }
}
