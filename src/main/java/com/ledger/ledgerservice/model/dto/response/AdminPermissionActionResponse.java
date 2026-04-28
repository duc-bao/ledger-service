package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminPermissionActionResponse {
    private String action;
    private boolean selected;
}
