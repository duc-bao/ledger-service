package com.ledger.ledgerservice.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTwoFactorRequest {
    @NotNull(message = "validation.twoFactor.enabled.required")
    private Boolean enabled;
}
