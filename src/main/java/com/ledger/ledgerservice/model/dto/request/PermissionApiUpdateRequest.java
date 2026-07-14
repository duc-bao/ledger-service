package com.ledger.ledgerservice.model.dto.request;

import com.ledger.ledgerservice.model.enums.PermissionMatchType;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request payload for updating a permission API mapping")
public class PermissionApiUpdateRequest {
    @Schema(description = "Permission identifier", example = "permission-id")
    private String permissionId;

    @Size(max = 10, message = "error.invalid")
    @Schema(description = "HTTP method", example = "GET")
    private String httpMethod;

    @Size(max = 300, message = "error.invalid")
    @Schema(description = "Normalized URI pattern", example = "/api/v1/users/{id}")
    private String uriPattern;

    @Schema(description = "Matching strategy", example = "EXACT")
    private PermissionMatchType matchType;

    @Size(max = 50, message = "error.invalid")
    @Schema(description = "Service code", example = "LEDGER_SERVICE")
    private String serviceCode;

    @Schema(description = "Status", example = "ACTIVE")
    private RecordStatus status;

    @Schema(description = "Priority", example = "0")
    private Integer priority;

    @Schema(description = "Whether this mapping allows public access", example = "false")
    private Boolean isAllow;

    @Schema(description = "Description")
    private String description;
}