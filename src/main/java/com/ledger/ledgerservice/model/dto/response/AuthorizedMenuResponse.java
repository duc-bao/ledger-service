package com.ledger.ledgerservice.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Authorized menu tree item")
public class AuthorizedMenuResponse {
    @Schema(description = "Menu identifier", example = "menu-id")
    private String id;

    @Schema(description = "Menu code", example = "USER_MANAGEMENT")
    private String code;

    @Schema(description = "Menu display name", example = "Quan ly nguoi dung")
    private String name;

    @Schema(description = "Frontend path", example = "/users")
    private String path;

    @Schema(description = "Menu icon", example = "users")
    private String icon;

    @Schema(description = "Parent menu identifier", example = "parent-menu-id")
    private String parentId;

    @Schema(description = "Sort order", example = "1")
    private Integer offset;

    @Schema(description = "Permission codes available on this menu")
    private List<String> permissions;

    @Schema(description = "UI actions available on this menu")
    private List<MenuActionResponse> actions;

    @Schema(description = "Child menus")
    private List<AuthorizedMenuResponse> children;
}