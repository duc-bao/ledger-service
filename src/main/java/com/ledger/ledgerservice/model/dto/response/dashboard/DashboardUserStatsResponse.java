package com.ledger.ledgerservice.model.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardUserStatsResponse {
    private StatItem totalUsers;
    private StatItem activeUsers;
    private StatItem inactiveUsers;
    private StatItem lockedUsers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatItem {
        private long value;
        private long differenceFromLastMonth;
        private double growthRatePercentage;
        private String criteria;
    }
}
