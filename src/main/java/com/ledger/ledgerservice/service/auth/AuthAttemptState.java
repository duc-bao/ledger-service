package com.ledger.ledgerservice.service.auth;

public record AuthAttemptState(int remainingAttempts, int maxAttempts, long lockSeconds, boolean locked) {
    public static AuthAttemptState failed(int remainingAttempts, int maxAttempts) {
        return new AuthAttemptState(remainingAttempts, maxAttempts, 0, false);
    }

    public static AuthAttemptState locked(int remainingAttempts, int maxAttempts, long lockSeconds) {
        return new AuthAttemptState(remainingAttempts, maxAttempts, lockSeconds, true);
    }
}
