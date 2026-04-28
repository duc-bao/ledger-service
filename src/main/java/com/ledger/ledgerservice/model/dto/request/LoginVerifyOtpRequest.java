package com.ledger.ledgerservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginVerifyOtpRequest {
    @NotBlank(message = "validation.username.required")
    private String username;

    @NotBlank(message = "validation.otp.required")
    @Pattern(regexp = "^\\d{6}$", message = "validation.otp.invalidFormat")
    private String otp;
}
