package com.ledger.ledgerservice.service.dashboard;

import com.ledger.ledgerservice.model.dto.response.dashboard.DashboardRoleDistributionResponse;
import com.ledger.ledgerservice.model.dto.response.dashboard.DashboardUserStatsResponse;

import java.util.List;

public interface DashboardAdminService {
    DashboardUserStatsResponse getUserStats();
    List<DashboardRoleDistributionResponse> getRoleDistribution();
}
