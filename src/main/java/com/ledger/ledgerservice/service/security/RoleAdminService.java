package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.model.dto.request.CreateRoleRequest;
import com.ledger.ledgerservice.model.dto.request.UpdateRoleRequest;
import com.ledger.ledgerservice.model.dto.response.RoleResponse;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoleAdminService {
    RoleResponse createRole(CreateRoleRequest request);

    Page<RoleResponse> searchRoles(String keyword, RecordStatus status, Pageable pageable);

    RoleResponse updateRole(String roleId, UpdateRoleRequest request);

    void deleteRole(String roleId);
}
