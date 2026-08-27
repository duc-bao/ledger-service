package com.ledger.ledgerservice.model.dto.response;

import com.ledger.ledgerservice.model.enums.RecordStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleResponse {
    private String id;
    private String code;
    private String name;
    private String description;
    private Boolean isDefault;
    private Boolean isSuperAdmin;
    private Integer sortOrder;
    private RecordStatus status;
}
