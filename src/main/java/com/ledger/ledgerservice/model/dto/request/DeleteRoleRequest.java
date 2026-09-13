package com.ledger.ledgerservice.model.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeleteRoleRequest {
    @Size(max = 500)
    private String deleteReason;
}
