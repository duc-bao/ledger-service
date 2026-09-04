package com.ledger.ledgerservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateRoleRequest {
    @NotBlank
    @Size(max = 50)
    private String code;

    @NotBlank
    private String name;

    @Size(max = 500)
    private String description;

    private Integer sortOrder;
}
