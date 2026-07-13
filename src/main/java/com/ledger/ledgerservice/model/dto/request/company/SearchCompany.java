package com.ledger.ledgerservice.model.dto.request.company;

import com.ledger.ledgerservice.model.dto.request.SearchBaseRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SearchCompany extends SearchBaseRequest {
    private String companyType;
}
