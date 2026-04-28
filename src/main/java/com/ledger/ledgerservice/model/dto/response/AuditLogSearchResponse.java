package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuditLogSearchResponse {
    private List<AuditLogItemResponse> items;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
