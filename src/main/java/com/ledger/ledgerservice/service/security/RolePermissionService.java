package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.model.dto.request.RolePermissionCreateRequest;
import com.ledger.ledgerservice.model.dto.request.RolePermissionReplaceRequest;
import com.ledger.ledgerservice.model.dto.response.PermissionResponse;
import com.ledger.ledgerservice.model.dto.response.RolePermissionResponse;

import java.util.List;

public interface RolePermissionService {
    RolePermissionResponse assign(RolePermissionCreateRequest request);
    void revoke(String rolePermissionId);
    List<PermissionResponse> getPermissionsByRole(String roleId);
    void replaceRolePermissions(String roleId, RolePermissionReplaceRequest request);
}