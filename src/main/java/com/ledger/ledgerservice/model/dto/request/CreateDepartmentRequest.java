package com.ledger.ledgerservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateDepartmentRequest {
    @NotBlank(message = "validation.department.code.required")
    @Size(max = 50)
    private String code;

    @NotBlank(message = "validation.department.name.required")
    @Size(max = 255)
    private String name;

    @Size(max = 100)
    private String shortName;

    private String parentId;
    private Integer sortOrder;
    private String managerUserId;
}
