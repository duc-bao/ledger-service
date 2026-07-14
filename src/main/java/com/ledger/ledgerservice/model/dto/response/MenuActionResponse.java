package com.ledger.ledgerservice.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "UI action exposed for a menu")
public class MenuActionResponse {
    @Schema(description = "Action code", example = "VIEW")
    private String code;

    @Schema(description = "Permission code backing this action", example = "USER_VIEW")
    private String permissionCode;
}