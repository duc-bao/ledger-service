package com.ledger.ledgerservice.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Request payload for replacing permissions of a role")
public class RolePermissionReplaceRequest {
    @NotNull(message = "error.invalid")
    @Schema(description = "Target permission identifiers", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> permissionIds;
}