package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginTokenResponse {
    private String accessToken;
    private String tokenType;
    private long expiresInSeconds;
    private boolean twoFactorRequired;
    private boolean isActiveCaptcha;
    private String fullName;
    private String roleName;
    private String departmentName;
    private int remainingAttempts;
    private long lockSeconds;
}
