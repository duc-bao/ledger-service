package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.request.RolePermissionCreateRequest;
import com.ledger.ledgerservice.model.dto.request.RolePermissionReplaceRequest;
import com.ledger.ledgerservice.model.dto.response.PermissionResponse;
import com.ledger.ledgerservice.model.dto.response.RolePermissionResponse;
import com.ledger.ledgerservice.model.entity.Group;
import com.ledger.ledgerservice.model.entity.Permission;
import com.ledger.ledgerservice.model.entity.RolePermission;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.model.mapper.PermissionMapper;
import com.ledger.ledgerservice.model.mapper.RolePermissionMapper;
import com.ledger.ledgerservice.repository.GroupRepository;
import com.ledger.ledgerservice.repository.PermissionRepository;
import com.ledger.ledgerservice.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {
    private final RolePermissionRepository rolePermissionRepository;
    private final GroupRepository groupRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional
    public RolePermissionResponse assign(RolePermissionCreateRequest request) {
        Group group = getActiveRoleOrThrow(request.getGroupId());
        Permission permission = getActivePermissionOrThrow(request.getPermissionId());
        if (rolePermissionRepository.existsByGroupIdAndPermissionId(group.getId(), permission.getId())) {
            throw new BusinessException(MessageCode.ROLE_PERMISSION_EXISTS, HttpStatus.CONFLICT);
        }

        RolePermission entity = rolePermissionRepository.save(RolePermission.builder()
                .groupId(group.getId())
                .permissionId(permission.getId())
                .status(RecordStatus.ACTIVE)
                .build());
        RolePermissionResponse response = rolePermissionMapper.toResponse(entity);
        enrich(response, group, permission);
        return response;
    }

    @Override
    @Transactional
    public void revoke(String rolePermissionId) {
        RolePermission entity = rolePermissionRepository.findById(rolePermissionId)
                .orElseThrow(() -> new BusinessException(MessageCode.ROLE_PERMISSION_NOT_FOUND, HttpStatus.NOT_FOUND));
        rolePermissionRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissionsByRole(String roleId) {
        getActiveRoleOrThrow(roleId);
        return permissionMapper.toResponses(permissionRepository.findActivePermissionsByRoleId(roleId));
    }

    @Override
    @Transactional
    public void replaceRolePermissions(String roleId, RolePermissionReplaceRequest request) {
        getActiveRoleOrThrow(roleId);
        Set<String> targetPermissionIds = request.getPermissionIds() == null ? Set.of() : new HashSet<>(request.getPermissionIds());
        Map<String, Permission> permissionsById = permissionRepository.findAllByIdIn(targetPermissionIds).stream()
                .collect(Collectors.toMap(Permission::getId, permission -> permission));
        if (permissionsById.size() != targetPermissionIds.size()) {
            throw new BusinessException(MessageCode.PERMISSION_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        if (permissionsById.values().stream().anyMatch(permission -> permission.getStatus() != RecordStatus.ACTIVE)) {
            throw new BusinessException(MessageCode.PERMISSION_STATUS_INVALID, HttpStatus.BAD_REQUEST);
        }

        List<RolePermission> currentMappings = rolePermissionRepository.findByGroupId(roleId);
        Map<String, RolePermission> currentByPermissionId = new HashMap<>();
        for (RolePermission mapping : currentMappings) {
            currentByPermissionId.put(mapping.getPermissionId(), mapping);
        }

        List<RolePermission> additions = new ArrayList<>();
        for (String permissionId : targetPermissionIds) {
            if (!currentByPermissionId.containsKey(permissionId)) {
                additions.add(RolePermission.builder().groupId(roleId).permissionId(permissionId).status(RecordStatus.ACTIVE).build());
            }
        }
        List<RolePermission> removals = currentMappings.stream()
                .filter(mapping -> !targetPermissionIds.contains(mapping.getPermissionId()))
                .toList();
        if (!removals.isEmpty()) {
            rolePermissionRepository.deleteAll(removals);
        }
        if (!additions.isEmpty()) {
            rolePermissionRepository.saveAll(additions);
        }
    }

    private Group getActiveRoleOrThrow(String roleId) {
        return groupRepository.findById(roleId)
                .filter(group -> group.getStatus() == RecordStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(MessageCode.GROUP_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private Permission getActivePermissionOrThrow(String permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new BusinessException(MessageCode.PERMISSION_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (permission.getStatus() != RecordStatus.ACTIVE) {
            throw new BusinessException(MessageCode.PERMISSION_STATUS_INVALID, HttpStatus.BAD_REQUEST);
        }
        return permission;
    }

    private void enrich(RolePermissionResponse response, Group group, Permission permission) {
        response.setGroupCode(group.getCode());
        response.setGroupName(group.getName());
        response.setPermissionCode(permission.getCode());
        response.setPermissionName(permission.getName());
        response.setModuleCode(permission.getModuleCode());
        response.setActionCode(permission.getActionCode());
    }
}