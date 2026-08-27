package com.ledger.ledgerservice.service.department;

import com.ledger.ledgerservice.model.dto.request.CreateDepartmentRequest;
import com.ledger.ledgerservice.model.dto.request.UpdateDepartmentRequest;
import com.ledger.ledgerservice.model.dto.response.DepartmentResponse;

public interface DepartmentAdminService {
    DepartmentResponse createDepartment(CreateDepartmentRequest request);

    DepartmentResponse updateDepartment(String departmentId, UpdateDepartmentRequest request);
}
