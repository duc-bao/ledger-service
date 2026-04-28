package com.ledger.ledgerservice.model.dto.request;

import com.ledger.ledgerservice.model.enums.UserStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class AdminUserSearchRequest extends SearchBaseRequest {
    private UserStatus status;
}

