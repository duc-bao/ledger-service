package com.ledger.ledgerservice.exception;

public class TransientExportException extends RuntimeException {
    public TransientExportException(String message) {
        super(message);
    }

    public TransientExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
