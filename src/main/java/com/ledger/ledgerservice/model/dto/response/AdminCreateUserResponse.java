package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminCreateUserResponse {
    private String userId;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private String userType;
    private Boolean requireChange;
    private String groupId;
}
