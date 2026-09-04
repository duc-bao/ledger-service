package com.ledger.ledgerservice.model.dto.request.company;

import com.ledger.ledgerservice.model.enums.CompanyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyStatusRequest {
    @NotNull(message = "error.company.statusInvalid")
    private CompanyStatus status;
}
