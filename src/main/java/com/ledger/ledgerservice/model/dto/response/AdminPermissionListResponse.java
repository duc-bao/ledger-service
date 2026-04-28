package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminPermissionListResponse {
    private List<AdminPermissionSummaryResponse> items;
}
