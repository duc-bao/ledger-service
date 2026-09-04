package com.ledger.ledgerservice.model.dto.request;

import com.ledger.ledgerservice.model.enums.MenuType;
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
@Schema(description = "Search criteria for menus")
public class MenuSearchRequest extends SearchBaseRequest {
    @Schema(description = "Parent menu identifier filter", example = "parent-menu-id")
    private String parentId;

    @Schema(description = "Menu type filter", example = "MENU")
    private MenuType menuType;

    @Schema(description = "Visibility filter", example = "true")
    private Boolean visible;

    @Schema(description = "Status filter", example = "ACTIVE")
    private RecordStatus status;
}
