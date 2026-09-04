package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthAttemptResponse {
    private int remainingAttempts;
    private int maxAttempts;
    private long lockSeconds;
    private boolean locked;
}
