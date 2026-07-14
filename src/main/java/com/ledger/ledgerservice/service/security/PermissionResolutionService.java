package com.ledger.ledgerservice.service.security;

import java.util.Set;

public interface PermissionResolutionService {
    Set<String> getGlobalPermissions(String userId);
    Set<String> getDepartmentPermissions(String userId, String departmentId);
    boolean hasPermission(String userId, String permissionCode, String departmentId);
    boolean hasApiPermission(String userId, String method, String uri);
}
