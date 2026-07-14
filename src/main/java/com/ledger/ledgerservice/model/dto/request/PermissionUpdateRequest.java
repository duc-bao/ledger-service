package com.ledger.ledgerservice.model.dto.request;

import com.ledger.ledgerservice.model.enums.RecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request payload for updating an RBAC permission")
public class PermissionUpdateRequest {
    @Size(max = 100, message = "error.code.invalid")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "error.code.invalid")
    @Schema(description = "Business permission code", example = "USER_CREATE")
    private String code;

    @Size(max = 200, message = "error.name.invalid")
    @Schema(description = "Permission display name", example = "Create user")
    private String name;

    @Size(max = 50, message = "error.code.invalid")
    @Schema(description = "Module code", example = "USER_MANAGEMENT")
    private String moduleCode;

    @Size(max = 50, message = "error.code.invalid")
    @Schema(description = "Action code", example = "CREATE")
    private String actionCode;

    @Size(max = 50, message = "error.code.invalid")
    @Schema(description = "Resource type", example = "USER")
    private String resourceType;

    @Schema(description = "Permission status", example = "ACTIVE")
    private RecordStatus status;

    @Size(max = 500, message = "error.invalid")
    @Schema(description = "Description", example = "Allow creating new users")
    private String description;
}