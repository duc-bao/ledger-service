package com.ledger.ledgerservice.config.feign.decoder;

import lombok.Getter;

@Getter
public class FeignBusinessByPassException extends RuntimeException {
    private final int status;
    private final byte[] body;
    private final String contentType;

    public FeignBusinessByPassException(int status, byte[] body, String contentType) {
        this.status = status;
        this.body = body;
        this.contentType = contentType;
    }

}
