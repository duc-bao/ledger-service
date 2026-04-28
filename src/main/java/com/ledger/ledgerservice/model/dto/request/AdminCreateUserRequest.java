package com.ledger.ledgerservice.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminCreateUserRequest {
    @NotBlank
    @Size(max = 50)
    private String username;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @Email
    @Size(max = 50)
    private String email;

    @Size(max = 15)
    private String phone;

    @Size(max = 100)
    private String fullName;

    @Size(max = 100)
    private String userType;

    private Boolean requireChange;

    private String groupId;
}
