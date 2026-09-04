package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DepartmentResponse {
    private String id;
    private String code;
    private String name;
    private String shortName;
    private String parentId;
    private String ancestors;
    private Integer sortOrder;
    private Integer treeLevel;
    private String managerUserId;
    private String status;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
