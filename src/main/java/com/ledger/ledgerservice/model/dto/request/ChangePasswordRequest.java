package com.ledger.ledgerservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "validation.currentPassword.required")
    private String currentPassword;

    @NotBlank(message = "validation.newPassword.required")
    @Size(min = 8, max = 100, message = "validation.newPassword.length")
    private String newPassword;

    @NotBlank(message = "validation.confirmPassword.required")
    private String confirmPassword;
}
