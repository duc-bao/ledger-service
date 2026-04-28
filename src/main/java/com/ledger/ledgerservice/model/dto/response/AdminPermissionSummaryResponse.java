package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminPermissionSummaryResponse {
    private String groupId;
    private String groupCode;
    private String groupName;
    private Boolean isDefault;
    private Boolean isSuperAdmin;
    private int totalMenusAssigned;
    private int totalActionsAssigned;
    private LocalDateTime updatedAt;
}
