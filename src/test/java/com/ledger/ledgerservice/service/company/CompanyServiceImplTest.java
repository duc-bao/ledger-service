package com.ledger.ledgerservice.service.company;

import com.ledger.ledgerservice.model.dto.response.company.CompanyResponse;
import com.ledger.ledgerservice.model.entity.Company;
import com.ledger.ledgerservice.model.enums.CompanyStatus;
import com.ledger.ledgerservice.model.enums.CompanyType;
import com.ledger.ledgerservice.model.enums.UnitLevel;
import com.ledger.ledgerservice.model.mapper.CompanyMapper;
import com.ledger.ledgerservice.repository.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CompanyMapper companyMapper;

    @InjectMocks
    private CompanyServiceImpl service;

    @Test
    void changeStatusLocksAndActivatesCompany() {
        Company company = new Company();
        company.setId("company-1");
        company.setName("Company");
        company.setCompanyType(CompanyType.TONG_CONG_TY);
        company.setUnitLevel(UnitLevel.TONG_CONG_TY);
        company.setStatus(CompanyStatus.ACTIVE);
        when(companyRepository.findById("company-1")).thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(companyMapper.mapToCompany(any(Company.class))).thenAnswer(invocation -> {
            Company saved = invocation.getArgument(0);
            return CompanyResponse.builder().id(saved.getId()).status(saved.getStatus().name()).build();
        });

        CompanyResponse locked = service.changeStatus("company-1", CompanyStatus.INACTIVE);
        CompanyResponse active = service.changeStatus("company-1", CompanyStatus.ACTIVE);

        assertEquals("INACTIVE", locked.getStatus());
        assertEquals("ACTIVE", active.getStatus());
    }
}
