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
@Schema(description = "Request payload for assigning a role to a department membership")
public class DepartmentUserRoleCreateRequest {
    @NotBlank(message = "error.department.userNotFound")
    @Schema(description = "Department membership identifier", example = "department-user-id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String departmentUserId;

    @NotBlank(message = "error.access.groupNotFound")
    @Schema(description = "Role identifier", example = "group-id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String groupId;
}