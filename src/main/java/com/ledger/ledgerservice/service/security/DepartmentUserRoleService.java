package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.model.dto.request.DepartmentUserRoleCreateRequest;
import com.ledger.ledgerservice.model.dto.response.DepartmentUserRoleResponse;
import com.ledger.ledgerservice.model.dto.response.PermissionResponse;

import java.util.List;

public interface DepartmentUserRoleService {
    DepartmentUserRoleResponse assign(DepartmentUserRoleCreateRequest request);

    void revoke(String departmentUserRoleId);

    List<DepartmentUserRoleResponse> getByUserAndDepartment(String userId, String departmentId);

    List<PermissionResponse> getPermissionsByUserAndDepartment(String userId, String departmentId);
}