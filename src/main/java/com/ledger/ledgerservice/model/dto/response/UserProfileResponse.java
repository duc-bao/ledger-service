package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {
    private String id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String userType;
    private Boolean requireChange;
    private Boolean twoFactorEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
