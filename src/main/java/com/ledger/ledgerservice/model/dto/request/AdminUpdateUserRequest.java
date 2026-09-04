package com.ledger.ledgerservice.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUpdateUserRequest {
    @Email(message = "validation.email.invalid")
    @Size(max = 50, message = "validation.email.length")
    private String email;

    @Size(max = 15, message = "validation.phone.length")
    private String phone;

    @Size(max = 100, message = "validation.fullName.length")
    private String fullName;

    @Size(max = 100)
    private String userType;

    private Boolean requireChange;

    private Boolean twoFactorEnabled;
}
