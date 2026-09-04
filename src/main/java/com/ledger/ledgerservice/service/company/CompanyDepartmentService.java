package com.ledger.ledgerservice.service.company;

import com.ledger.ledgerservice.model.dto.request.company.AssignCompanyDepartmentsRequest;
import com.ledger.ledgerservice.model.dto.response.company.CompanyDepartmentAssignmentResponse;

public interface CompanyDepartmentService {
    CompanyDepartmentAssignmentResponse assignDepartments(String companyId, AssignCompanyDepartmentsRequest request);
}
