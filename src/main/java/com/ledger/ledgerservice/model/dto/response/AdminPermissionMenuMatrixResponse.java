package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminPermissionMenuMatrixResponse {
    private String menuId;
    private String menuCode;
    private String displayName;
    private String parentId;
    private String path;
    private String icon;
    private int offset;
    private List<AdminPermissionActionResponse> actions;
}
