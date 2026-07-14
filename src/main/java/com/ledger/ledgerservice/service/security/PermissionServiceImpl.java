package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.request.PermissionCreateRequest;
import com.ledger.ledgerservice.model.dto.request.PermissionSearchRequest;
import com.ledger.ledgerservice.model.dto.request.PermissionUpdateRequest;
import com.ledger.ledgerservice.model.dto.response.PageResponse;
import com.ledger.ledgerservice.model.dto.response.PermissionResponse;
import com.ledger.ledgerservice.model.entity.Permission;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.model.mapper.PermissionMapper;
import com.ledger.ledgerservice.repository.MenuPermissionRepository;
import com.ledger.ledgerservice.repository.PermissionApiRepository;
import com.ledger.ledgerservice.repository.PermissionRepository;
import com.ledger.ledgerservice.repository.RolePermissionRepository;
import com.ledger.ledgerservice.repository.specification.PermissionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9_]+$");

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionApiRepository permissionApiRepository;
    private final MenuPermissionRepository menuPermissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional
    public PermissionResponse create(PermissionCreateRequest request) {
        String normalizedCode = normalizeCode(request.getCode());
        if (permissionRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new BusinessException(MessageCode.PERMISSION_CODE_EXISTS, HttpStatus.CONFLICT);
        }

        Permission permission = permissionMapper.toEntity(request);
        permission.setCode(normalizedCode);
        permission.setName(trimRequired(request.getName(), MessageCode.NAME_INVALID));
        permission.setModuleCode(normalizeUpper(request.getModuleCode()));
        permission.setActionCode(normalizeUpper(request.getActionCode()));
        permission.setResourceType(normalizeUpperNullable(request.getResourceType()));
        permission.setDescription(trimNullable(request.getDescription()));
        permission.setStatus(RecordStatus.ACTIVE);
        return permissionMapper.toResponse(permissionRepository.save(permission));
    }

    @Override
    @Transactional
    public PermissionResponse update(String permissionId, PermissionUpdateRequest request) {
        Permission permission = getPermissionOrThrow(permissionId);
        String normalizedCode = request.getCode() == null ? permission.getCode() : normalizeCode(request.getCode());
        if (permissionRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, permissionId)) {
            throw new BusinessException(MessageCode.PERMISSION_CODE_EXISTS, HttpStatus.CONFLICT);
        }

        permissionMapper.updateEntity(request, permission);
        permission.setCode(normalizedCode);
        if (request.getName() != null) {
            permission.setName(trimRequired(request.getName(), MessageCode.NAME_INVALID));
        }
        if (request.getModuleCode() != null) {
            permission.setModuleCode(normalizeUpper(request.getModuleCode()));
        }
        if (request.getActionCode() != null) {
            permission.setActionCode(normalizeUpper(request.getActionCode()));
        }
        if (request.getResourceType() != null) {
            permission.setResourceType(normalizeUpperNullable(request.getResourceType()));
        }
        if (request.getDescription() != null) {
            permission.setDescription(trimNullable(request.getDescription()));
        }
        if (request.getStatus() != null) {
            permission.setStatus(request.getStatus());
        }
        return permissionMapper.toResponse(permissionRepository.save(permission));
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getById(String permissionId) {
        return permissionMapper.toResponse(getPermissionOrThrow(permissionId));
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getByCode(String code) {
        return permissionRepository.findByCodeIgnoreCase(normalizeCode(code))
                .map(permissionMapper::toResponse)
                .orElseThrow(() -> new BusinessException(MessageCode.PERMISSION_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PermissionResponse> search(PermissionSearchRequest request, Pageable pageable) {
        Pageable resolved = resolvePageable(request, pageable);
        Page<Permission> page = permissionRepository.findAll(PermissionSpecification.bySearchRequest(request), resolved);
        return PageResponse.<PermissionResponse>builder()
                .items(permissionMapper.toResponses(page.getContent()))
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    @Override
    @Transactional
    public void delete(String permissionId) {
        Permission permission = getPermissionOrThrow(permissionId);
        if (rolePermissionRepository.existsByPermissionId(permissionId)
                || permissionApiRepository.existsByPermissionId(permissionId)
                || menuPermissionRepository.existsByPermissionId(permissionId)) {
            throw new BusinessException(MessageCode.PERMISSION_IN_USE, HttpStatus.CONFLICT);
        }
        permissionRepository.delete(permission);
    }

    @Override
    @Transactional
    public void changeStatus(String permissionId, RecordStatus status) {
        if (status == null) {
            throw new BusinessException(MessageCode.PERMISSION_STATUS_INVALID, HttpStatus.BAD_REQUEST);
        }
        Permission permission = getPermissionOrThrow(permissionId);
        permission.setStatus(status);
        permissionRepository.save(permission);
    }

    private Permission getPermissionOrThrow(String permissionId) {
        return permissionRepository.findById(permissionId)
                .orElseThrow(() -> new BusinessException(MessageCode.PERMISSION_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private Pageable resolvePageable(PermissionSearchRequest request, Pageable pageable) {
        if (pageable != null && pageable.isPaged()) {
            return pageable;
        }
        String sortField = request != null && StringUtils.hasText(request.getSort()) ? request.getSort() : "createdAt";
        Sort.Direction direction = request != null && "ASC".equalsIgnoreCase(request.getOrder()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        int page = request != null && request.getPage() != null && request.getPage() > 0 ? request.getPage() - 1 : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }

    private String normalizeCode(String code) {
        String normalized = normalizeUpper(code);
        if (!CODE_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException(MessageCode.CODE_INVALID, HttpStatus.BAD_REQUEST);
        }
        return normalized;
    }

    private String normalizeUpper(String value) {
        String normalized = trimRequired(value, MessageCode.CODE_INVALID);
        return normalized.toUpperCase();
    }

    private String normalizeUpperNullable(String value) {
        String trimmed = trimNullable(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }

    private String trimRequired(String value, MessageCode messageCode) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(messageCode, HttpStatus.BAD_REQUEST);
        }
        return value.trim();
    }

    private String trimNullable(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}