package com.ledger.ledgerservice.exception;

import com.ledger.ledgerservice.model.enums.MessageCode;
import org.springframework.http.HttpStatus;

public class BusinessExportException extends BusinessException {
    public BusinessExportException(MessageCode messageCode) {
        super(messageCode);
    }

    public BusinessExportException(MessageCode messageCode, HttpStatus statusCode) {
        super(messageCode, statusCode);
    }

    public BusinessExportException(MessageCode messageCode, String paramKey) {
        super(messageCode, paramKey);
    }
}
