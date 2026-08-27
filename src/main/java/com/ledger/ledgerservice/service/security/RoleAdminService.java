package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.model.dto.request.CreateRoleRequest;
import com.ledger.ledgerservice.model.dto.response.RoleResponse;

public interface RoleAdminService {
    RoleResponse createRole(CreateRoleRequest request);

    void deleteRole(String roleId);
}
