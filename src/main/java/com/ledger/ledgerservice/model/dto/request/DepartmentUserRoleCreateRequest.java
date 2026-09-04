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
public class DepartmentUserRoleCreateRequest {
    @NotBlank(message = "error.http.notFound")
    @Schema(description = "Department identifier", example = "department-id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String departmentId;

    @NotBlank(message = "error.auth.userNotFound")
    @Schema(description = "User identifier", example = "user-id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;

    @NotBlank(message = "error.access.groupNotFound")
    @Schema(description = "Role identifier", example = "group-id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String groupId;
}