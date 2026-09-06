package com.ledger.ledgerservice.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public final class AesGcmCryptoUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final String PREFIX = "ENC:";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final SecretKey SECRET_KEY = initSecretKey();

    private AesGcmCryptoUtil() {
    }

    private static SecretKey initSecretKey() {
        String secret = System.getenv("EMAIL_ENCRYPTION_SECRET");
        if (!StringUtils.hasText(secret)) {
            secret = System.getProperty("EMAIL_ENCRYPTION_SECRET");
        }
        if (!StringUtils.hasText(secret)) {
            secret = "ledger-service-smtp-encryption-default-secret-key-32b";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, "AES");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public static String encrypt(String plainText) {
        if (!StringUtils.hasText(plainText)) {
            return plainText;
        }
        if (plainText.startsWith(PREFIX)) {
            return plainText; // Already encrypted
        }

        try {
            byte[] iv = new byte[IV_LENGTH_BYTE];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY, parameterSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return PREFIX + Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception ex) {
            log.error("Failed to encrypt text with AES-GCM", ex);
            throw new IllegalStateException("Encryption failure", ex);
        }
    }

    public static String decrypt(String cipherTextWithPrefix) {
        if (!StringUtils.hasText(cipherTextWithPrefix)) {
            return cipherTextWithPrefix;
        }
        if (!cipherTextWithPrefix.startsWith(PREFIX)) {
            return cipherTextWithPrefix; // Backward compatibility for unencrypted legacy rows
        }

        try {
            String base64Content = cipherTextWithPrefix.substring(PREFIX.length());
            byte[] decoded = Base64.getDecoder().decode(base64Content);

            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY, parameterSpec);

            byte[] plainTextBytes = cipher.doFinal(cipherText);
            return new String(plainTextBytes, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            log.error("Failed to decrypt text with AES-GCM", ex);
            throw new IllegalStateException("Decryption failure", ex);
        }
    }
}
