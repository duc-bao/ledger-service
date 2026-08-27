package com.ledger.ledgerservice.model.dto.response;

import com.ledger.ledgerservice.model.enums.MenuType;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Menu response")
public class MenuResponse {
    private String id;
    private String code;
    private String name;
    private String path;
    private List<String> ancestors;
    private String parentId;
    private String parentCode;
    private String parentName;
    private String icon;
    private List<String> actions;
    private Integer offset;
    private MenuType menuType;
    private String component;
    private Boolean visible;
    private RecordStatus status;
    private String externalUrl;
    private String description;
    private List<String> permissionIds;
    private List<String> permissionCodes;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
