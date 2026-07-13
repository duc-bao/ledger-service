package com.ledger.ledgerservice.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequestOtpRequest {
    @NotBlank(message = "validation.email.required")
    @Email(message = "validation.email.invalid")
    private String email;
}
