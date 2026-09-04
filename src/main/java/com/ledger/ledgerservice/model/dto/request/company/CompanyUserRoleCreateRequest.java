package com.ledger.ledgerservice.model.dto.request.company;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanyUserRoleCreateRequest {
    @NotBlank
    private String companyId;

    @NotBlank
    private String userId;

    @NotBlank
    private String groupId;
}
