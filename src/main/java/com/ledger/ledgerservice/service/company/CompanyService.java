package com.ledger.ledgerservice.service.company;

import com.ledger.ledgerservice.model.dto.request.company.CreateCompanyRequest;
import com.ledger.ledgerservice.model.dto.request.company.SearchCompany;
import com.ledger.ledgerservice.model.dto.response.company.CompanyResponse;
import com.ledger.ledgerservice.model.dto.response.company.CompanyTreeResponse;

import java.util.List;

public interface CompanyService {
    CompanyResponse createCompany(CreateCompanyRequest request);

    CompanyResponse getById(String companyId);

    List<CompanyTreeResponse> search(SearchCompany searchCompany);

    List<CompanyTreeResponse> getCompanyTree();

    CompanyResponse updateCompany(String companyId, CreateCompanyRequest request);
}
