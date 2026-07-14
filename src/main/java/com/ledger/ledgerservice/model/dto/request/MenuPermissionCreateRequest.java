package com.ledger.ledgerservice.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Request payload for assigning a permission to a menu")
public class MenuPermissionCreateRequest {
    @NotBlank(message = "error.access.menuNotFound")
    @Schema(description = "Menu identifier", example = "menu-id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String menuId;

    @NotBlank(message = "error.access.permissionNotFound")
    @Schema(description = "Permission identifier", example = "permission-id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String permissionId;

    @Size(max = 50, message = "error.invalid")
    @Schema(description = "Optional UI action code", example = "VIEW")
    private String displayAction;
}