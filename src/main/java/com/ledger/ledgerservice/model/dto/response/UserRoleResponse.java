package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRoleResponse {
    private String userId;
    private String groupId;
}
