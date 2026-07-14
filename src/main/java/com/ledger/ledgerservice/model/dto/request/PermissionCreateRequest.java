package com.ledger.ledgerservice.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
@Schema(description = "Request payload for creating an RBAC permission")
public class PermissionCreateRequest {
    @NotBlank(message = "error.code.invalid")
    @Size(max = 100, message = "error.code.invalid")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "error.code.invalid")
    @Schema(description = "Business permission code", example = "USER_CREATE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @NotBlank(message = "error.name.invalid")
    @Size(max = 200, message = "error.name.invalid")
    @Schema(description = "Permission display name", example = "Create user", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "error.code.invalid")
    @Size(max = 50, message = "error.code.invalid")
    @Schema(description = "Module code", example = "USER_MANAGEMENT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moduleCode;

    @NotBlank(message = "error.code.invalid")
    @Size(max = 50, message = "error.code.invalid")
    @Schema(description = "Action code", example = "CREATE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String actionCode;

    @Size(max = 50, message = "error.code.invalid")
    @Schema(description = "Optional resource type", example = "USER")
    private String resourceType;

    @Size(max = 500, message = "error.invalid")
    @Schema(description = "Optional description", example = "Allow creating new users")
    private String description;
}