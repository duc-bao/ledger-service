package com.ledger.ledgerservice.model.dto.request.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCompanyRequest {
    @NotBlank(message = "validation.company.name.required")
    private String name;

    @NotBlank(message = "validation.company.type.required")
    private String companyType;

    private String parentId;

    private String shortName;

    @NotBlank(message = "validation.company.unitLevel.required")
    private String unitLevel;
}
