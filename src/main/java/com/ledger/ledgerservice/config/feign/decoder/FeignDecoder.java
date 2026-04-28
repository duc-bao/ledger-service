package com.ledger.ledgerservice.config.feign.decoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

@Slf4j
public class FeignDecoder implements Decoder {
    private static final int MAX_BODY_BYTES = 2 * 1024 * 1024;
    private final ObjectMapper objectMapper;

    public FeignDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
        if (response.body() == null) {
            log.warn(
                    "[FEIGN] Response body is NULL. status={} method={} url={}",
                    response.status(),
                    response.request() != null ? response.request().httpMethod() : "NA",
                    response.request() != null ? response.request().url() : "NA"
            );
            return null;
        }

        byte[] bytes = Util.toByteArray(response.body().asInputStream());
        if (bytes.length > MAX_BODY_BYTES) {
            throw new IOException("Feign response too large: " + bytes.length);
        }

        if (bytes.length == 0) {
            log.warn("[FEIGN] 2xx but empty body. status={} url={}",
                    response.status(),
                    response.request() != null ? response.request().url() : "NA");
            return null;
        }

        try {
            return objectMapper.readValue(bytes, objectMapper.constructType(type));
        } catch (Exception ex) {
            String raw = new String(bytes, StandardCharsets.UTF_8);
            log.error("[FEIGN] Decode failed. status={} url={} raw={}",
                    response.status(),
                    response.request() != null ? response.request().url() : "NA",
                    raw);
            throw ex;
        }
    }
}
