package com.ledger.ledgerservice.service.excel;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExportFilePayload {
    private String fileName;
    private byte[] content;
}
