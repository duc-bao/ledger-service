package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleWithPermissionsResponse {
    private String groupId;
    private String code;
    private String name;
    private int totalMenusAssigned;
    private int totalActionsAssigned;
}
