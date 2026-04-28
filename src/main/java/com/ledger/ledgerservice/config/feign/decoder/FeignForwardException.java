package com.ledger.ledgerservice.config.feign.decoder;


import lombok.Getter;

@Getter
public final class FeignForwardException extends RuntimeException {

    private final int status;
    private  String feignClient;
    private  String feignMethod;
    private  String url;
    private  String httpMethod;
    private final byte[] body;
    private final String contentType;

    public FeignForwardException(
            int status,
            String feignClient,
            String feignMethod,
            String url,
            String httpMethod,
            byte[] body,
            String contentType
    ) {
        super(null, null, false, false);
        this.status = status;
        this.feignClient = feignClient;
        this.feignMethod = feignMethod;
        this.url = url;
        this.httpMethod = httpMethod;
        this.body = body;
        this.contentType = contentType;
    }

    public FeignForwardException(int status, byte[] body, String contentType) {
        this.status = status;
        this.body = body;
        this.contentType = contentType;
    }
}

