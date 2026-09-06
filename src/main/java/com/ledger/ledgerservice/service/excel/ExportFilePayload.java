package com.ledger.ledgerservice.service.excel;

import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.Resource;

@Data
@Builder
public class ExportFilePayload {
    private String fileName;
    private Resource resource;
    private long contentLength;
}
