package com.ledger.ledgerservice.model.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentExportRow {
    private String code;
    private String name;
    private String shortName;
    private String parentId;
    private String status;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
