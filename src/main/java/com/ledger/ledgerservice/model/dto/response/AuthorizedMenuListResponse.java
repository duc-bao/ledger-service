package com.ledger.ledgerservice.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authorized menu list response")
public class AuthorizedMenuListResponse {
    @Schema(description = "User identifier", example = "user-id")
    private String userId;

    @Schema(description = "Username", example = "admin")
    private String username;

    @Schema(description = "Authorized menu tree")
    private List<AuthorizedMenuResponse> menus;
}