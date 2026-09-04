package com.ledger.ledgerservice.model.dto.response.company;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CompanyDepartmentAssignmentResponse {
    private String companyId;
    private List<String> departmentIds;
}
