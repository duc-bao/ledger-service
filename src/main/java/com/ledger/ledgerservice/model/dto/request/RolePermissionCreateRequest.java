package com.ledger.ledgerservice.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Request payload for assigning a permission to a role")
public class RolePermissionCreateRequest {
    @NotBlank(message = "error.access.groupIdRequired")
    @Schema(description = "Role identifier", example = "role-id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String groupId;

    @NotBlank(message = "error.access.permissionNotFound")
    @Schema(description = "Permission identifier", example = "permission-id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String permissionId;
}