package com.ledger.ledgerservice.model.mapper;

import com.ledger.ledgerservice.model.dto.response.company.CompanyResponse;
import com.ledger.ledgerservice.model.entity.Company;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    CompanyResponse mapToCompany(Company company);
}
