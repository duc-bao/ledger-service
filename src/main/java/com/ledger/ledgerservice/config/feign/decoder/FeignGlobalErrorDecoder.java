package com.ledger.ledgerservice.config.feign.decoder;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Builder
@Getter
@Setter
@AllArgsConstructor
@Slf4j
public class FeignGlobalErrorDecoder implements ErrorDecoder {
    /**
     * Giới hạn body lỗi để tránh OOM / DoS
     */
    private static final int MAX_ERROR_BODY_SIZE = 2 * 1024 * 1024;
    private static final int BUFFER_SIZE = 4096;

    /**
     * Các API trả 400 nhưng vẫn là BUSINESS FLOW
     */
    private static final Set<String> BUSINESS_400_WHITELIST = Set.of();

    @Override
    public Exception decode(String s, Response response) {
        byte[] body = safeReadBody(response.body());
        String contentType = extractContentType(response.headers());
        if ((response.status() == 400) && isWhitelisted(s)) {

            log.warn("[FEIGN BYPASS {}] method={}, forward raw response",
                    response.status(), s);

            return new FeignBusinessByPassException(
                    response.status(),
                    body,
                    contentType
            );
        }
        String client = extractClient(s);
        String method = extractMethod(s);

        String url = response.request() != null ? response.request().url() : null;
        String httpMethod = response.request() != null
                ? response.request().httpMethod().name()
                : null;

        log.error("""
                        [FEIGN ERROR]
                        Client      : {}
                        Method      : {}
                        HTTP Method : {}
                        URL         : {}
                        Status      : {}
                        BodySize    : {}
                        """,
                client,
                method,
                httpMethod,
                url,
                response.status(),
                body.length
        );

        return new FeignForwardException(
                response.status(),
                client,
                method,
                url,
                httpMethod,
                body,
                contentType
        );
    }

    private String extractContentType(Map<String, Collection<String>> headers) {
        Collection<String> values = headers.get("Content-Type");
        return (values == null || values.isEmpty())
                ? null
                : values.iterator().next();
    }

    /**
     * Đọc InputStream với giới hạn cứng để tránh OOM / DoS
     */
    private byte[] safeReadBody(Response.Body body) {
        if (body == null) {
            return new byte[0];
        }

        try (InputStream is = body.asInputStream();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            byte[] chunk = new byte[BUFFER_SIZE];
            int total = 0;
            int read;

            while ((read = is.read(chunk)) != -1) {
                total += read;
                if (total > MAX_ERROR_BODY_SIZE) {
                    log.warn(
                            "Feign error body exceeded {} bytes, truncated",
                            MAX_ERROR_BODY_SIZE
                    );
                    break;
                }
                buffer.write(chunk, 0, read);
            }
            return buffer.toByteArray();

        } catch (Exception ex) {
            log.warn(
                    "Cannot safely read feign error body: {}",
                    ex.getMessage()
            );
            return new byte[0];
        }
    }

    private boolean isWhitelisted(String methodKey) {
        return BUSINESS_400_WHITELIST
                .stream()
                .anyMatch(methodKey::startsWith);
    }


    private String extractClient(String methodKey) {
        int idx = methodKey.indexOf('#');
        return idx > 0 ? methodKey.substring(0, idx) : "UNKNOWN";
    }

    private String extractMethod(String methodKey) {
        int idx = methodKey.indexOf('#');
        return idx > 0 ? methodKey.substring(idx + 1) : "UNKNOWN";
    }
}
