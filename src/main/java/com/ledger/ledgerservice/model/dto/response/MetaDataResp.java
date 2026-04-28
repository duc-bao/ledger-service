package com.ledger.ledgerservice.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MetaDataResp {
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
