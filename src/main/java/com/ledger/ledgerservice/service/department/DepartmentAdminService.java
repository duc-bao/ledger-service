package com.ledger.ledgerservice.service.department;

import com.ledger.ledgerservice.model.dto.request.CreateDepartmentRequest;
import com.ledger.ledgerservice.model.dto.request.DepartmentSearchRequest;
import com.ledger.ledgerservice.model.dto.request.UpdateDepartmentRequest;
import com.ledger.ledgerservice.model.dto.response.DepartmentResponse;
import com.ledger.ledgerservice.model.dto.response.DepartmentTreeResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DepartmentAdminService {
    DepartmentResponse createDepartment(CreateDepartmentRequest request);

    DepartmentResponse updateDepartment(String departmentId, UpdateDepartmentRequest request);

    DepartmentResponse getDepartment(String departmentId);

    Page<DepartmentResponse> searchDepartments(DepartmentSearchRequest request);

    List<DepartmentTreeResponse> getDepartmentTree();
}
