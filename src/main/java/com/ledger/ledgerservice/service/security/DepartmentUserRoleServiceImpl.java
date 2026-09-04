package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.model.dto.request.DepartmentUserRoleCreateRequest;
import com.ledger.ledgerservice.model.dto.response.DepartmentUserRoleResponse;
import com.ledger.ledgerservice.model.dto.response.PermissionResponse;
import com.ledger.ledgerservice.model.entity.DepartmentEntity;
import com.ledger.ledgerservice.model.entity.DepartmentUserEntity;
import com.ledger.ledgerservice.model.entity.DepartmentUserRole;
import com.ledger.ledgerservice.model.entity.Group;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.model.enums.UserStatus;
import com.ledger.ledgerservice.model.mapper.DepartmentUserRoleMapper;
import com.ledger.ledgerservice.model.mapper.PermissionMapper;
import com.ledger.ledgerservice.repository.DepartmentRepository;
import com.ledger.ledgerservice.repository.DepartmentUserRepository;
import com.ledger.ledgerservice.repository.DepartmentUserRoleRepository;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentUserRoleServiceImpl implements DepartmentUserRoleService {
    private final DepartmentUserRoleRepository departmentUserRoleRepository;
    private final DepartmentUserRepository departmentUserRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PermissionRepository permissionRepository;
    private final DepartmentUserRoleMapper departmentUserRoleMapper;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional
    public DepartmentUserRoleResponse assign(DepartmentUserRoleCreateRequest request) {
        DepartmentEntity department = getActiveDepartmentOrThrow(request.getDepartmentId());
        User user = getActiveUserOrThrow(request.getUserId());
        Group group = getActiveRoleOrThrow(request.getGroupId());

        DepartmentUserEntity membership = departmentUserRepository
                .findByDepartmentIdAndUserId(department.getId(), user.getId())
                .map(existing -> {
                    if (existing.getStatus() != RecordStatus.ACTIVE) {
                        existing.setStatus(RecordStatus.ACTIVE);
                        existing.setLeftDate(null);
                        existing.setUpdatedAt(LocalDateTime.now());
                        existing.setUpdatedBy(resolveActor());
                        return departmentUserRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> departmentUserRepository.save(DepartmentUserEntity.builder()
                        .departmentId(department.getId())
                        .userId(user.getId())
                        .isPrimary(departmentUserRepository.findByUserIdAndStatus(user.getId(), RecordStatus.ACTIVE).isEmpty())
                        .status(RecordStatus.ACTIVE)
                        .joinDate(LocalDateTime.now())
                        .createdBy(resolveActor())
                        .updatedBy(resolveActor())
                        .build()));

        Optional<DepartmentUserRole> existingRoleOpt = departmentUserRoleRepository
                .findByDepartmentUserIdAndGroupId(membership.getId(), group.getId());

        DepartmentUserRole entity;
        if (existingRoleOpt.isPresent()) {
            DepartmentUserRole existingRole = existingRoleOpt.get();
            if (RecordStatus.ACTIVE.equals(existingRole.getStatus())) {
                throw new BusinessException(MessageCode.DEPARTMENT_USER_ROLE_EXISTS, HttpStatus.CONFLICT);
            }
            existingRole.setStatus(RecordStatus.ACTIVE);
            existingRole.setEffectiveTo(null);
            existingRole.setUpdatedAt(LocalDateTime.now());
            existingRole.setUpdatedBy(resolveActor());
            entity = departmentUserRoleRepository.save(existingRole);
        } else {
            entity = departmentUserRoleRepository.save(DepartmentUserRole.builder()
                    .departmentUserId(membership.getId())
                    .groupId(group.getId())
                    .status(RecordStatus.ACTIVE)
                    .createdBy(resolveActor())
                    .updatedBy(resolveActor())
                    .build());
        }

        return toResponse(entity, membership, group);
    }

    @Override
    @Transactional
    public void revoke(String departmentUserRoleId) {
        DepartmentUserRole entity = departmentUserRoleRepository.findById(departmentUserRoleId)
                .orElseThrow(() -> new BusinessException(MessageCode.DEPARTMENT_USER_ROLE_NOT_FOUND, HttpStatus.NOT_FOUND));
        departmentUserRoleRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentUserRoleResponse> getByUserAndDepartment(String userId, String departmentId) {
        DepartmentUserEntity membership = departmentUserRepository.findByDepartmentIdAndUserIdAndStatus(departmentId, userId, RecordStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(MessageCode.DEPARTMENT_USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        return departmentUserRoleRepository.findByDepartmentUserId(membership.getId()).stream()
                .map(entity -> toResponse(entity, membership, groupRepository.findById(entity.getGroupId()).orElse(null)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissionsByUserAndDepartment(String userId, String departmentId) {
        departmentUserRepository.findByDepartmentIdAndUserIdAndStatus(departmentId, userId, RecordStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(MessageCode.DEPARTMENT_USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        return permissionMapper.toResponses(permissionRepository.findActiveDepartmentPermissions(userId, departmentId));
    }

    private DepartmentEntity getActiveDepartmentOrThrow(String departmentId) {
        DepartmentEntity department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException(MessageCode.NOT_FOUND, HttpStatus.NOT_FOUND));
        if (!Boolean.TRUE.equals(department.getIsActive()) && !"ACTIVE".equalsIgnoreCase(department.getStatus())) {
            throw new BusinessException(MessageCode.NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        return department;
    }

    private User getActiveUserOrThrow(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(MessageCode.USER_INACTIVE, HttpStatus.FORBIDDEN);
        }
        return user;
    }

    private Group getActiveRoleOrThrow(String groupId) {
        return groupRepository.findByIdAndStatus(groupId, RecordStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(MessageCode.GROUP_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private String resolveActor() {
        return RequestContextHolder.get() != null && StringUtils.hasText(RequestContextHolder.get().getUsername())
                ? RequestContextHolder.get().getUsername()
                : "system";
    }

    private DepartmentUserRoleResponse toResponse(DepartmentUserRole entity, DepartmentUserEntity membership, Group group) {
        DepartmentUserRoleResponse response = departmentUserRoleMapper.toResponse(entity);
        response.setDepartmentId(membership.getDepartmentId());
        response.setUserId(membership.getUserId());
        if (group != null) {
            response.setGroupCode(group.getCode());
            response.setGroupName(group.getName());
        }
        return response;
    }
}