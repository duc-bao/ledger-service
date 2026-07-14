package com.ledger.ledgerservice.model.dto.request;

import com.ledger.ledgerservice.model.enums.PermissionMatchType;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Request payload for creating a permission API mapping")
public class PermissionApiCreateRequest {
    @NotBlank(message = "error.access.permissionNotFound")
    @Schema(description = "Permission identifier", example = "permission-id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String permissionId;

    @NotBlank(message = "error.invalid")
    @Size(max = 10, message = "error.invalid")
    @Schema(description = "HTTP method", example = "GET", requiredMode = Schema.RequiredMode.REQUIRED)
    private String httpMethod;

    @NotBlank(message = "error.invalid")
    @Size(max = 300, message = "error.invalid")
    @Schema(description = "Normalized URI pattern", example = "/api/v1/users/{id}", requiredMode = Schema.RequiredMode.REQUIRED)
    private String uriPattern;

    @NotNull(message = "error.invalid")
    @Schema(description = "Matching strategy", example = "ANT_PATH", requiredMode = Schema.RequiredMode.REQUIRED)
    private PermissionMatchType matchType;

    @Size(max = 50, message = "error.invalid")
    @Schema(description = "Service code", example = "LEDGER_SERVICE")
    private String serviceCode;

    @Schema(description = "Status", example = "ACTIVE")
    private RecordStatus status;

    @Schema(description = "Priority", example = "10")
    private Integer priority;

    @Schema(description = "Whether this mapping allows public access", example = "false")
    private Boolean isAllow;

    @Schema(description = "Description")
    private String description;
}