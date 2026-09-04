package com.ledger.ledgerservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateDepartmentRequest {
    @NotBlank(message = "validation.department.name.required")
    @Size(max = 255)
    private String name;

    @Size(max = 100)
    private String shortName;

    private String parentId;
    private Integer sortOrder;
    private String managerUserId;
    private String status;
    private Boolean isActive;
}
