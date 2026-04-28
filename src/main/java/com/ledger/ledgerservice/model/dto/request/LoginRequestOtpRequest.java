package com.ledger.ledgerservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestOtpRequest {
    @NotBlank(message = "validation.username.required")
    private String username;

    @NotBlank(message = "validation.password.required")
    private String password;
}
