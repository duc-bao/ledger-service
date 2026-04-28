package com.ledger.ledgerservice.model.dto.request.email;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailAttachment {
    private String fileName;
    private String contentType;
    private byte[] content;
}
