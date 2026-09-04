package com.ledger.ledgerservice.service.company;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.model.dto.request.company.AssignCompanyDepartmentsRequest;
import com.ledger.ledgerservice.model.dto.response.company.CompanyDepartmentAssignmentResponse;
import com.ledger.ledgerservice.model.entity.CompanyDepartment;
import com.ledger.ledgerservice.model.entity.DepartmentEntity;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.repository.CompanyDepartmentRepository;
import com.ledger.ledgerservice.repository.CompanyRepository;
import com.ledger.ledgerservice.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyDepartmentServiceImpl implements CompanyDepartmentService {
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final CompanyDepartmentRepository companyDepartmentRepository;

    @Override
    @Transactional
    public CompanyDepartmentAssignmentResponse assignDepartments(String companyId, AssignCompanyDepartmentsRequest request) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(MessageCode.COMPANY_NOT_FOUND, HttpStatus.NOT_FOUND));
        Set<String> requestedIds = new LinkedHashSet<>();
        if (request.getDepartmentIds() != null) {
            for (String departmentId : request.getDepartmentIds()) {
                if (!StringUtils.hasText(departmentId)) {
                    continue;
                }
                DepartmentEntity department = departmentRepository.findById(departmentId.trim())
                        .orElseThrow(() -> new BusinessException(MessageCode.NOT_FOUND, HttpStatus.NOT_FOUND));
                requestedIds.add(department.getId());
            }
        }

        List<CompanyDepartment> existingMappings = companyDepartmentRepository.findByCompanyId(companyId);
        for (CompanyDepartment mapping : existingMappings) {
            mapping.setStatus(requestedIds.contains(mapping.getDepartmentId()) ? RecordStatus.ACTIVE : RecordStatus.INACTIVE);
            mapping.setUpdatedAt(LocalDateTime.now());
            mapping.setUpdatedBy(resolveActor());
            companyDepartmentRepository.save(mapping);
        }

        Set<String> existingDepartmentIds = existingMappings.stream().map(CompanyDepartment::getDepartmentId).collect(Collectors.toSet());
        for (String departmentId : requestedIds) {
            if (existingDepartmentIds.contains(departmentId)) {
                continue;
            }
            companyDepartmentRepository.save(CompanyDepartment.builder()
                    .companyId(companyId)
                    .departmentId(departmentId)
                    .status(RecordStatus.ACTIVE)
                    .createdBy(resolveActor())
                    .updatedBy(resolveActor())
                    .build());
        }

        return CompanyDepartmentAssignmentResponse.builder()
                .companyId(companyId)
                .departmentIds(List.copyOf(requestedIds))
                .build();
    }

    private String resolveActor() {
        return RequestContextHolder.get() != null && StringUtils.hasText(RequestContextHolder.get().getUsername())
                ? RequestContextHolder.get().getUsername()
                : "system";
    }
}
