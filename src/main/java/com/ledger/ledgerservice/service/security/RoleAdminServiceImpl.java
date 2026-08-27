package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.model.dto.request.CreateRoleRequest;
import com.ledger.ledgerservice.model.dto.response.RoleResponse;
import com.ledger.ledgerservice.model.entity.Group;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.repository.DepartmentUserRoleRepository;
import com.ledger.ledgerservice.repository.GroupRepository;
import com.ledger.ledgerservice.repository.RolePermissionRepository;
import com.ledger.ledgerservice.repository.UserGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RoleAdminServiceImpl implements RoleAdminService {
    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final DepartmentUserRoleRepository departmentUserRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        String code = trimRequired(request.getCode()).toUpperCase(Locale.ROOT);
        if (groupRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException(MessageCode.GROUP_CODE_EXISTS, HttpStatus.CONFLICT);
        }
        if (groupRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BusinessException(MessageCode.GROUP_NAME_EXISTS, HttpStatus.CONFLICT);
        }

        Group group = groupRepository.save(Group.builder()
                .code(code)
                .name(request.getName().trim())
                .description(trimNullable(request.getDescription()))
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .status(RecordStatus.ACTIVE)
                .isDefault(Boolean.FALSE)
                .isSuperAdmin(Boolean.FALSE)
                .createdBy(resolveActor())
                .updatedBy(resolveActor())
                .build());
        return toResponse(group);
    }

    @Override
    @Transactional
    public void deleteRole(String roleId) {
        Group group = groupRepository.findByIdAndStatus(roleId, RecordStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(MessageCode.GROUP_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (Boolean.TRUE.equals(group.getIsSuperAdmin()) || Boolean.TRUE.equals(group.getIsDefault())) {
            throw new BusinessException(MessageCode.CONFLICT, HttpStatus.CONFLICT);
        }
        if (!userGroupRepository.findByGroupIdAndStatus(roleId, RecordStatus.ACTIVE).isEmpty()
                || departmentUserRoleRepository.existsByGroupId(roleId)
                || !rolePermissionRepository.findByGroupIdAndStatus(roleId, RecordStatus.ACTIVE).isEmpty()) {
            throw new BusinessException(MessageCode.CONFLICT, HttpStatus.CONFLICT);
        }

        group.setStatus(RecordStatus.INACTIVE);
        group.setUpdatedAt(LocalDateTime.now());
        group.setUpdatedBy(resolveActor());
        groupRepository.save(group);
    }

    private RoleResponse toResponse(Group group) {
        return RoleResponse.builder()
                .id(group.getId())
                .code(group.getCode())
                .name(group.getName())
                .description(group.getDescription())
                .isDefault(group.getIsDefault())
                .isSuperAdmin(group.getIsSuperAdmin())
                .sortOrder(group.getSortOrder())
                .status(group.getStatus())
                .build();
    }

    private String trimRequired(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(MessageCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        }
        return value.trim();
    }

    private String trimNullable(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String resolveActor() {
        return RequestContextHolder.get() != null && StringUtils.hasText(RequestContextHolder.get().getUsername())
                ? RequestContextHolder.get().getUsername()
                : "system";
    }
}
