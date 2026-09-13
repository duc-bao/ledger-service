package com.ledger.ledgerservice.model.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardRoleDistributionResponse {
    private String roleId;
    private String roleCode;
    private String roleName;
    private Long userCount;
    private Double percentage;
}
