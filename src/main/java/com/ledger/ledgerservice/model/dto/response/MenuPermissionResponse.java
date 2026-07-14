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
@Schema(description = "Menu-permission mapping response")
public class MenuPermissionResponse {
    private String id;
    private String menuId;
    private String menuCode;
    private String menuName;
    private String permissionId;
    private String permissionCode;
    private String permissionName;
    private String displayAction;
    private RecordStatus status;
    private String description;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}