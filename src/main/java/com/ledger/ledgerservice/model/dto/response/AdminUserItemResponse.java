package com.ledger.ledgerservice.model.dto.response;

import com.ledger.ledgerservice.model.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserItemResponse {
    private String id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String userType;
    private Boolean requireChange;
    private UserStatus status;
    private String groupId;
    private String groupCode;
    private String groupName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

