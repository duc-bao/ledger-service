package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminPermissionDetailResponse {
    private String groupId;
    private String groupCode;
    private String groupName;
    private boolean hasPermissions;
    private List<AdminPermissionMenuMatrixResponse> permissionMatrix;
}
