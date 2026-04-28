package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthorizedMenuListResponse {
    private String userId;
    private String username;
    private List<AuthorizedMenuResponse> menus;
}
