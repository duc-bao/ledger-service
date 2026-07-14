package com.ledger.ledgerservice.model.dto.response;

import com.ledger.ledgerservice.model.enums.RecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "RBAC permission response")
public class PermissionResponse {
    @Schema(description = "Permission identifier", example = "a4d3fda3-0da2-4b0b-a89e-6ae0f0f799d2")
    private String id;

    @Schema(description = "Business permission code", example = "USER_CREATE")
    private String code;

    @Schema(description = "Permission name", example = "Create user")
    private String name;

    @Schema(description = "Module code", example = "USER_MANAGEMENT")
    private String moduleCode;

    @Schema(description = "Action code", example = "CREATE")
    private String actionCode;

    @Schema(description = "Resource type", example = "USER")
    private String resourceType;

    @Schema(description = "Status", example = "ACTIVE")
    private RecordStatus status;

    @Schema(description = "Description", example = "Allow creating new users")
    private String description;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Created by")
    private String createdBy;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Last updated by")
    private String updatedBy;
}