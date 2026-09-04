package com.ledger.ledgerservice.model.dto.response.company;

import com.ledger.ledgerservice.model.enums.RecordStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CompanyUserRoleResponse {
    private String id;
    private String companyId;
    private String userId;
    private String groupId;
    private String groupCode;
    private String groupName;
    private RecordStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
