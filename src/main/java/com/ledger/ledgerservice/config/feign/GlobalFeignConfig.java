package com.ledger.ledgerservice.config.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.ledgerservice.config.feign.decoder.FeignDecoder;
import com.ledger.ledgerservice.config.feign.decoder.FeignTimingLogger;
import com.ledger.ledgerservice.util.SSLUtils;
import feign.Client;
import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import javax.net.ssl.X509TrustManager;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class GlobalFeignConfig {
    private final Environment env;
    private static final int MAX_REQUESTS = 500;
    private static final int MAX_REQUESTS_PER_HOST = 300;

    private static final int MAX_IDLE_CONNECTIONS = 400;
    private static final int KEEP_ALIVE_SECOND = 20;
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 15000;
    private static final int WRITE_TIMEOUT_MS = 15000;

    @Bean
    public Request.Options requestOption() {
        return new Request.Options(
                CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS,
                READ_TIMEOUT_MS, TimeUnit.MILLISECONDS,
                true
        );
    }

    @Bean
    public Retryer retryer() {
        return Retryer.NEVER_RETRY;
    }

    @Bean
    public Logger.Level feignLoggerLevelFull() {
        return isProd() ? Logger.Level.BASIC : Logger.Level.FULL;
    }

    @Bean
    public Logger feignLogger() {
        return new FeignTimingLogger();
    }

    @Bean
    public Client feignClient() throws Exception {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(MAX_REQUESTS);
        dispatcher.setMaxRequestsPerHost(MAX_REQUESTS_PER_HOST);

        okhttp3.OkHttpClient.Builder builder = new okhttp3.OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectionPool(
                        new ConnectionPool(
                                MAX_IDLE_CONNECTIONS,
                                KEEP_ALIVE_SECOND, TimeUnit.SECONDS
                        )
                )
                .connectTimeout(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true);

        if (!isProd()) {
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    // Intentionally left empty.
                    // This TrustManager is used ONLY in non-production environments
                    // to allow self-signed certificates during local development.
                    // MUST NOT be enabled in production.
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    // Intentionally left empty.
                    // This TrustManager is used ONLY in non-production environments
                    // to allow self-signed certificates during local development.
                    // MUST NOT be enabled in production.
                }
            };

            builder.sslSocketFactory(SSLUtils.disableSSLCertValidation(), trustManager)
                    .hostnameVerifier(SSLUtils.disableHostnameVerifier());
        }

        return new feign.okhttp.OkHttpClient(builder.build());
    }

    private boolean isProd() {
        return env.acceptsProfiles(Profiles.of("prod"));
    }

    @Bean
    public Decoder feignDecoder(ObjectMapper objectMapper) {
        return new FeignDecoder(objectMapper);
    }

}
