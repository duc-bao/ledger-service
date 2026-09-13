package com.ledger.ledgerservice.service.dashboard;

import com.ledger.ledgerservice.model.dto.response.dashboard.DashboardRoleDistributionResponse;
import com.ledger.ledgerservice.model.dto.response.dashboard.DashboardUserStatsResponse;
import com.ledger.ledgerservice.model.entity.Group;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.entity.UserGroup;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.model.enums.UserStatus;
import com.ledger.ledgerservice.repository.GroupRepository;
import com.ledger.ledgerservice.repository.UserGroupRepository;
import com.ledger.ledgerservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardAdminServiceImpl implements DashboardAdminService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardUserStatsResponse getUserStats() {
        List<User> allUsers = userRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfCurrentMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime thirtyDaysAgo = now.minusDays(30);

        // 1. Total Users
        long totalUsers = allUsers.size();
        long usersBeforeThisMonth = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isBefore(startOfCurrentMonth))
                .count();
        long totalDiff = totalUsers - usersBeforeThisMonth;
        double totalGrowth = usersBeforeThisMonth > 0 ? Math.round(((double) totalDiff * 1000.0) / usersBeforeThisMonth) / 10.0 : 0.0;

        // 2. Active Users (logged in within last 30 days and ACTIVE status)
        long activeUsers = allUsers.stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE && u.getLastLoginAt() != null && u.getLastLoginAt().isAfter(thirtyDaysAgo))
                .count();
        long activeBeforeThisMonth = allUsers.stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE && u.getCreatedAt() != null && u.getCreatedAt().isBefore(startOfCurrentMonth))
                .filter(u -> u.getLastLoginAt() != null && u.getLastLoginAt().isAfter(thirtyDaysAgo))
                .count();
        long activeDiff = activeUsers - activeBeforeThisMonth;
        double activeGrowth = activeBeforeThisMonth > 0 ? Math.round(((double) activeDiff * 1000.0) / activeBeforeThisMonth) / 10.0 : 0.0;

        // 3. Inactive Users (INACTIVE status, or ACTIVE status but never logged in or not logged in for 30+ days)
        long inactiveUsers = allUsers.stream()
                .filter(u -> u.getStatus() == UserStatus.INACTIVE ||
                        (u.getStatus() == UserStatus.ACTIVE && (u.getLastLoginAt() == null || !u.getLastLoginAt().isAfter(thirtyDaysAgo))))
                .count();
        long inactiveBeforeThisMonth = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isBefore(startOfCurrentMonth))
                .filter(u -> u.getStatus() == UserStatus.INACTIVE ||
                        (u.getStatus() == UserStatus.ACTIVE && (u.getLastLoginAt() == null || !u.getLastLoginAt().isAfter(thirtyDaysAgo))))
                .count();
        long inactiveDiff = inactiveUsers - inactiveBeforeThisMonth;
        double inactiveGrowth = inactiveBeforeThisMonth > 0 ? Math.round(((double) inactiveDiff * 1000.0) / inactiveBeforeThisMonth) / 10.0 : 0.0;

        // 4. Locked Users (LOCKED status)
        long lockedUsers = allUsers.stream()
                .filter(u -> u.getStatus() == UserStatus.LOCKED)
                .count();
        long lockedBeforeThisMonth = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isBefore(startOfCurrentMonth))
                .filter(u -> u.getStatus() == UserStatus.LOCKED)
                .count();
        long lockedDiff = lockedUsers - lockedBeforeThisMonth;
        double lockedGrowth = lockedBeforeThisMonth > 0 ? Math.round(((double) lockedDiff * 1000.0) / lockedBeforeThisMonth) / 10.0 : 0.0;

        return DashboardUserStatsResponse.builder()
                .totalUsers(DashboardUserStatsResponse.StatItem.builder()
                        .value(totalUsers)
                        .differenceFromLastMonth(totalDiff)
                        .growthRatePercentage(totalGrowth)
                        .criteria("Total registered users in system")
                        .build())
                .activeUsers(DashboardUserStatsResponse.StatItem.builder()
                        .value(activeUsers)
                        .differenceFromLastMonth(activeDiff)
                        .growthRatePercentage(activeGrowth)
                        .criteria("Active status and logged in within last 30 days")
                        .build())
                .inactiveUsers(DashboardUserStatsResponse.StatItem.builder()
                        .value(inactiveUsers)
                        .differenceFromLastMonth(inactiveDiff)
                        .growthRatePercentage(inactiveGrowth)
                        .criteria("Inactive status or no login for > 30 days")
                        .build())
                .lockedUsers(DashboardUserStatsResponse.StatItem.builder()
                        .value(lockedUsers)
                        .differenceFromLastMonth(lockedDiff)
                        .growthRatePercentage(lockedGrowth)
                        .criteria("Account locked due to security policy or admin action")
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardRoleDistributionResponse> getRoleDistribution() {
        List<Group> activeGroups = groupRepository.findByStatusOrderBySortOrderAscCodeAsc(RecordStatus.ACTIVE);
        if (activeGroups.isEmpty()) {
            return List.of();
        }

        List<String> groupIds = activeGroups.stream().map(Group::getId).toList();
        List<UserGroup> userGroups = userGroupRepository.findByGroupIdInAndStatus(groupIds, RecordStatus.ACTIVE);
        Map<String, Long> countMap = userGroups.stream()
                .collect(Collectors.groupingBy(UserGroup::getGroupId, Collectors.counting()));

        long totalAssigned = countMap.values().stream().mapToLong(Long::longValue).sum();

        List<DashboardRoleDistributionResponse> result = new ArrayList<>();
        for (Group group : activeGroups) {
            long count = countMap.getOrDefault(group.getId(), 0L);
            double percentage = totalAssigned > 0 ? Math.round(((double) count * 1000.0) / totalAssigned) / 10.0 : 0.0;
            result.add(DashboardRoleDistributionResponse.builder()
                    .roleId(group.getId())
                    .roleCode(group.getCode())
                    .roleName(group.getName())
                    .userCount(count)
                    .percentage(percentage)
                    .build());
        }
        return result;
    }
}
