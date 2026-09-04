package com.ledger.ledgerservice.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentTreeResponse {
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<DepartmentTreeResponse> children = new ArrayList<>();
}
