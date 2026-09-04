package com.ledger.ledgerservice.service.company;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.model.dto.request.company.CompanyUserRoleCreateRequest;
import com.ledger.ledgerservice.model.dto.response.PermissionResponse;
import com.ledger.ledgerservice.model.dto.response.company.CompanyUserRoleResponse;
import com.ledger.ledgerservice.model.entity.Company;
import com.ledger.ledgerservice.model.entity.CompanyUserRole;
import com.ledger.ledgerservice.model.entity.Group;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.enums.CompanyStatus;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.model.enums.UserStatus;
import com.ledger.ledgerservice.model.mapper.PermissionMapper;
import com.ledger.ledgerservice.repository.CompanyRepository;
import com.ledger.ledgerservice.repository.CompanyUserRoleRepository;
import com.ledger.ledgerservice.repository.GroupRepository;
import com.ledger.ledgerservice.repository.PermissionRepository;
import com.ledger.ledgerservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyUserRoleServiceImpl implements CompanyUserRoleService {
    private final CompanyUserRoleRepository companyUserRoleRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional
    public CompanyUserRoleResponse assign(CompanyUserRoleCreateRequest request) {
        Company company = getActiveCompanyOrThrow(request.getCompanyId());
        User user = getActiveUserOrThrow(request.getUserId());
        Group group = groupRepository.findByIdAndStatus(request.getGroupId(), RecordStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(MessageCode.GROUP_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (companyUserRoleRepository.existsByCompanyIdAndUserIdAndGroupId(company.getId(), user.getId(), group.getId())) {
            throw new BusinessException(MessageCode.ROLE_PERMISSION_EXISTS, HttpStatus.CONFLICT);
        }

        CompanyUserRole entity = CompanyUserRole.builder()
                .companyId(company.getId())
                .userId(user.getId())
                .groupId(group.getId())
                .status(RecordStatus.ACTIVE)
                .createdBy(resolveActor())
                .updatedBy(resolveActor())
                .build();
        return toResponse(companyUserRoleRepository.save(entity), group);
    }

    @Override
    @Transactional
    public void revoke(String companyUserRoleId) {
        CompanyUserRole entity = companyUserRoleRepository.findById(companyUserRoleId)
                .orElseThrow(() -> new BusinessException(MessageCode.ROLE_PERMISSION_NOT_FOUND, HttpStatus.NOT_FOUND));
        companyUserRoleRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyUserRoleResponse> getByCompanyAndUser(String companyId, String userId) {
        getActiveCompanyOrThrow(companyId);
        getActiveUserOrThrow(userId);
        return companyUserRoleRepository.findByCompanyIdAndUserIdAndStatus(companyId, userId, RecordStatus.ACTIVE).stream()
                .map(entity -> toResponse(entity, groupRepository.findById(entity.getGroupId()).orElse(null)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissions(String companyId, String userId) {
        getActiveCompanyOrThrow(companyId);
        getActiveUserOrThrow(userId);
        return permissionMapper.toResponses(permissionRepository.findActiveCompanyPermissions(userId, companyId));
    }

    private Company getActiveCompanyOrThrow(String companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(MessageCode.COMPANY_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (company.getStatus() == CompanyStatus.INACTIVE) {
            throw new BusinessException(MessageCode.COMPANY_STATUS_INVALID, HttpStatus.BAD_REQUEST);
        }
        return company;
    }

    private User getActiveUserOrThrow(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(MessageCode.USER_INACTIVE, HttpStatus.FORBIDDEN);
        }
        return user;
    }

    private CompanyUserRoleResponse toResponse(CompanyUserRole entity, Group group) {
        return CompanyUserRoleResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .userId(entity.getUserId())
                .groupId(entity.getGroupId())
                .groupCode(group != null ? group.getCode() : null)
                .groupName(group != null ? group.getName() : null)
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private String resolveActor() {
        return RequestContextHolder.get() != null && StringUtils.hasText(RequestContextHolder.get().getUsername())
                ? RequestContextHolder.get().getUsername()
                : "system";
    }
}
