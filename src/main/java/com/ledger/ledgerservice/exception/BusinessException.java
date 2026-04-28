package com.ledger.ledgerservice.exception;

import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.MessageParam;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final String code;
    private final String messageKey;
    private final String paramKey;
    private final HttpStatus statusCode;

    public BusinessException(MessageCode messageCode) {
        this(messageCode, (String) null, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(MessageCode messageCode, MessageParam messageParam) {
        this(messageCode, messageParam != null ? messageParam.getKey() : null, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(MessageCode messageCode, String paramKey) {
        this(messageCode, paramKey, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(MessageCode messageCode, HttpStatus statusCode) {
        this(messageCode, (String) null, statusCode);
    }

    public BusinessException(MessageCode messageCode, MessageParam messageParam, HttpStatus statusCode) {
        this(messageCode, messageParam != null ? messageParam.getKey() : null, statusCode);
    }

    public BusinessException(MessageCode messageCode, String paramKey, HttpStatus statusCode) {
        super(messageCode != null ? messageCode.getCode() : null);
        this.code = messageCode != null ? messageCode.getCode() : null;
        this.messageKey = messageCode != null ? messageCode.getKey() : null;
        this.paramKey = paramKey;
        this.statusCode = statusCode;
    }
}
