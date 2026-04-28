package com.ledger.ledgerservice.model.dto.request.email;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EmailTemplateModel {
    private List<EmailTemplateDefinition> templates = new ArrayList<>();
}
