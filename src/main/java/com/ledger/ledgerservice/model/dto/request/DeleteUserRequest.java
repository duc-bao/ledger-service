package com.ledger.ledgerservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeleteUserRequest {
    @NotBlank(message = "validation.password.required")
    private String adminPassword;

    @Size(max = 500)
    private String deleteReason;
}
