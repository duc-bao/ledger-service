package com.ledger.ledgerservice.service.company;

import com.ledger.ledgerservice.model.dto.request.company.CompanyUserRoleCreateRequest;
import com.ledger.ledgerservice.model.dto.response.PermissionResponse;
import com.ledger.ledgerservice.model.dto.response.company.CompanyUserRoleResponse;

import java.util.List;

public interface CompanyUserRoleService {
    CompanyUserRoleResponse assign(CompanyUserRoleCreateRequest request);

    void revoke(String companyUserRoleId);

    List<CompanyUserRoleResponse> getByCompanyAndUser(String companyId, String userId);

    List<PermissionResponse> getPermissions(String companyId, String userId);
}
