package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginTokenResponse {
    private String accessToken;
    private String tokenType;
    private long expiresInSeconds;
}
