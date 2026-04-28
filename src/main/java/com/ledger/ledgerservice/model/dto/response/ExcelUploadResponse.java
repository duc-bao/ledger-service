package com.ledger.ledgerservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExcelUploadResponse {
    private String originalFileName;
    private String storedFileName;
    private String storedPath;
    private long size;
}
