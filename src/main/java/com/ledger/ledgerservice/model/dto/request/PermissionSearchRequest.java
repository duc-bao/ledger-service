package com.ledger.ledgerservice.model.dto.request;

import com.ledger.ledgerservice.model.enums.RecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Search criteria for RBAC permissions")
public class PermissionSearchRequest extends SearchBaseRequest {
    @Schema(description = "Module code filter", example = "USER_MANAGEMENT")
    private String moduleCode;

    @Schema(description = "Action code filter", example = "CREATE")
    private String actionCode;

    @Schema(description = "Resource type filter", example = "USER")
    private String resourceType;

    @Schema(description = "Status filter", example = "ACTIVE")
    private RecordStatus status;
}