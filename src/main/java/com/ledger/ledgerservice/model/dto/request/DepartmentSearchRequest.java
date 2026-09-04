package com.ledger.ledgerservice.model.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DepartmentSearchRequest extends SearchBaseRequest {
    private String status;
    private Boolean isActive;
}
