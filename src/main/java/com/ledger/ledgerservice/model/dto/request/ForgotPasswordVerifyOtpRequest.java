package com.ledger.ledgerservice.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForgotPasswordVerifyOtpRequest {
    @NotBlank(message = "validation.email.required")
    @Email(message = "validation.email.invalid")
    private String email;

    @NotBlank(message = "validation.otp.required")
    @Pattern(regexp = "^\\d{6}$", message = "validation.otp.invalidFormat")
    private String otp;

    @NotBlank(message = "validation.newPassword.required")
    @Size(min = 8, max = 100, message = "validation.newPassword.length")
    private String newPassword;
}
