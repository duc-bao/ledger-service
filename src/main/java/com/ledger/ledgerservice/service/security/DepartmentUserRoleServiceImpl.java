package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.request.DepartmentUserRoleCreateRequest;
import com.ledger.ledgerservice.model.dto.response.DepartmentUserRoleResponse;
import com.ledger.ledgerservice.model.dto.response.PermissionResponse;
import com.ledger.ledgerservice.model.entity.DepartmentUserEntity;
import com.ledger.ledgerservice.model.entity.DepartmentUserRole;
import com.ledger.ledgerservice.model.entity.Group;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.model.mapper.DepartmentUserRoleMapper;
import com.ledger.ledgerservice.model.mapper.PermissionMapper;
import com.ledger.ledgerservice.repository.DepartmentUserRepository;
import com.ledger.ledgerservice.repository.DepartmentUserRoleRepository;
import com.ledger.ledgerservice.repository.GroupRepository;
import com.ledger.ledgerservice.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentUserRoleServiceImpl implements DepartmentUserRoleService {
    private final DepartmentUserRoleRepository departmentUserRoleRepository;
    private final DepartmentUserRepository departmentUserRepository;
    private final GroupRepository groupRepository;
    private final PermissionRepository permissionRepository;
    private final DepartmentUserRoleMapper departmentUserRoleMapper;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional
    public DepartmentUserRoleResponse assign(DepartmentUserRoleCreateRequest request) {
        DepartmentUserEntity membership = getActiveMembershipOrThrow(request.getDepartmentUserId());
        Group group = getActiveRoleOrThrow(request.getGroupId());
        if (departmentUserRoleRepository.existsByDepartmentUserIdAndGroupId(membership.getId(), group.getId())) {
            throw new BusinessException(MessageCode.DEPARTMENT_USER_ROLE_EXISTS, HttpStatus.CONFLICT);
        }

        DepartmentUserRole entity = departmentUserRoleRepository.save(DepartmentUserRole.builder()
                .departmentUserId(membership.getId())
                .groupId(group.getId())
                .status(RecordStatus.ACTIVE)
                .build());
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

    private DepartmentUserEntity getActiveMembershipOrThrow(String departmentUserId) {
        return departmentUserRepository.findByIdAndStatus(departmentUserId, RecordStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(MessageCode.DEPARTMENT_USER_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private Group getActiveRoleOrThrow(String groupId) {
        return groupRepository.findByIdAndStatus(groupId, RecordStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(MessageCode.GROUP_NOT_FOUND, HttpStatus.NOT_FOUND));
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