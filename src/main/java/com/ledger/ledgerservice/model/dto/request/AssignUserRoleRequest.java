package com.ledger.ledgerservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignUserRoleRequest {
    @NotBlank
    private String userId;

    @NotBlank
    private String groupId;
}
