package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class EffectivePermissionResponse {
    private String userId;
    private Set<String> authorities;
}
