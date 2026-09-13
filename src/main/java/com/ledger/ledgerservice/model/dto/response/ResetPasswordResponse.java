package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResetPasswordResponse {
    private String userId;
    private boolean emailSent;
    private String temporaryPassword;
}
