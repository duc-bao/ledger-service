package com.ledger.ledgerservice.model.dto.request.company;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AssignCompanyDepartmentsRequest {
    @NotNull
    private List<String> departmentIds;
}
