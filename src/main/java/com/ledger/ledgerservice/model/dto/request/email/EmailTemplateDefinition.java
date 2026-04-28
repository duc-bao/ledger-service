package com.ledger.ledgerservice.model.dto.request.email;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EmailTemplateDefinition {
    private String code;
    private String subject;
    private String template;
    private List<String> params = new ArrayList<>();
}
