package com.ledger.ledgerservice.util;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * SSL utilities for local/dev testing only.
 * <p>
 * ⚠ WARNING:
 * - This class disables SSL certificate and hostname verification.
 * - MUST NOT be used in production environments.
 */
public class SSLUtils {
    private SSLUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static SSLSocketFactory disableSSLCertValidation() {
        if (isProd()) {
            throw new UnsupportedOperationException(
                    "Disabling SSL validation is not allowed in production"
            );
        }
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                // Intentionally left empty.
                // This TrustManager is used ONLY for local/dev testing
                // to bypass SSL certificate validation.
                // MUST NOT be used in production.
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                // Intentionally left empty.
                // This TrustManager is used ONLY for local/dev testing
                // to bypass SSL certificate validation.
                // MUST NOT be used in production.
            }
        }};

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException("Failed to initialize SSL context", e);
        }
    }

    public static HostnameVerifier disableHostnameVerifier() {
        if (isProd()) {
            throw new UnsupportedOperationException(
                    "Disabling hostname verification is not allowed in production"
            );
        }
        return (hostname, session) -> true;
    }

    private static boolean isProd() {
        String profile = System.getProperty("spring.profiles.active");
        return "prod".equalsIgnoreCase(profile);
    }
}
