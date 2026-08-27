package com.ledger.ledgerservice.model.dto.request;

import com.ledger.ledgerservice.model.enums.MenuType;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Request payload for updating a menu")
public class MenuUpdateRequest {
    @Size(max = 150, message = "error.name.invalid")
    @Schema(description = "Menu display name", example = "User management")
    private String name;

    @Size(max = 255, message = "error.invalid")
    @Schema(description = "Frontend route path", example = "/users")
    private String path;

    @Schema(description = "Parent menu identifier", example = "parent-menu-id")
    private String parentId;

    @Size(max = 255, message = "error.invalid")
    @Schema(description = "Menu icon", example = "users")
    private String icon;

    @Schema(description = "Legacy action codes for UI compatibility", example = "[\"VIEW\", \"CREATE\"]")
    private List<String> actions;

    @Schema(description = "Sort order", example = "1")
    private Integer offset;

    @Schema(description = "Menu type", example = "MENU")
    private MenuType menuType;

    @Size(max = 200, message = "error.invalid")
    @Schema(description = "Frontend component path", example = "pages/users/index")
    private String component;

    @Schema(description = "Visibility flag", example = "true")
    private Boolean visible;

    @Schema(description = "Status", example = "ACTIVE")
    private RecordStatus status;

    @Size(max = 500, message = "error.invalid")
    @Schema(description = "External URL", example = "https://docs.example.com")
    private String externalUrl;

    @Size(max = 500, message = "error.invalid")
    @Schema(description = "Description", example = "Main menu for user administration")
    private String description;

    @Schema(description = "RBAC permission identifiers assigned to this menu")
    private List<String> permissionIds;
}
