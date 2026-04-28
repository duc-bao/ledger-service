package com.ledger.ledgerservice.model.dto.request.email;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailInlineResource {
    private String contentId;
    private String contentType;
    private byte[] content;
}
