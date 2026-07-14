package com.ledger.ledgerservice.model.dto.request;

import com.ledger.ledgerservice.model.enums.RecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Request payload for changing permission status")
public class PermissionStatusRequest {
    @NotNull(message = "error.access.permissionStatusInvalid")
    @Schema(description = "Target status", example = "INACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
    private RecordStatus status;
}