package com.ledger.ledgerservice.controller;

import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.dto.response.dashboard.DashboardRoleDistributionResponse;
import com.ledger.ledgerservice.model.dto.response.dashboard.DashboardUserStatsResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.service.dashboard.DashboardAdminService;
import com.ledger.ledgerservice.util.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard Admin", description = "APIs for admin dashboard analytics")
@SecurityRequirement(name = "bearerAuth")
public class DashboardAdminController {

    private final DashboardAdminService dashboardAdminService;
    private final ResponseHelper responseHelper;

    @GetMapping("/user-stats")
    @Operation(summary = "Get user statistics for dashboard cards")
    public ResponseEntity<BaseResponse<DashboardUserStatsResponse>> getUserStats() {
        DashboardUserStatsResponse data = dashboardAdminService.getUserStats();
        return responseHelper.ok(MessageCode.SUCCESS, data, HttpStatus.OK);
    }

    @GetMapping("/role-distribution")
    @Operation(summary = "Get user distribution across roles for dashboard chart")
    public ResponseEntity<BaseResponse<List<DashboardRoleDistributionResponse>>> getRoleDistribution() {
        List<DashboardRoleDistributionResponse> data = dashboardAdminService.getRoleDistribution();
        return responseHelper.ok(MessageCode.SUCCESS, data, HttpStatus.OK);
    }
}
