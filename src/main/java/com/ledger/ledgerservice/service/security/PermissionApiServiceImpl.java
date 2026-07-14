package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.config.properties.AppSettingProperty;
import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.request.PermissionApiCreateRequest;
import com.ledger.ledgerservice.model.dto.request.PermissionApiSearchRequest;
import com.ledger.ledgerservice.model.dto.request.PermissionApiUpdateRequest;
import com.ledger.ledgerservice.model.dto.response.PageResponse;
import com.ledger.ledgerservice.model.dto.response.PermissionApiResponse;
import com.ledger.ledgerservice.model.entity.Permission;
import com.ledger.ledgerservice.model.entity.PermissionApi;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.PermissionMatchType;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.model.mapper.PermissionApiMapper;
import com.ledger.ledgerservice.repository.PermissionApiRepository;
import com.ledger.ledgerservice.repository.PermissionRepository;
import com.ledger.ledgerservice.repository.specification.PermissionApiSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PermissionApiServiceImpl implements PermissionApiService {
    private final PermissionApiRepository permissionApiRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionApiMapper permissionApiMapper;
    private final AppSettingProperty appSettingProperty;

    @Override
    @Transactional
    public PermissionApiResponse create(PermissionApiCreateRequest request) {
        Permission permission = getPermissionOrThrow(request.getPermissionId());
        validatePermissionActive(permission);
        String method = normalizeMethod(request.getHttpMethod());
        String uri = normalizeUri(request.getUriPattern());
        String serviceCode = normalizeServiceCode(request.getServiceCode());
        if (permissionApiRepository.existsByPermissionIdAndHttpMethodIgnoreCaseAndUriPatternAndServiceCodeIgnoreCase(permission.getId(), method, uri, serviceCode)) {
            throw new BusinessException(MessageCode.PERMISSION_API_EXISTS, HttpStatus.CONFLICT);
        }

        PermissionApi entity = permissionApiMapper.toEntity(request);
        entity.setPermissionId(permission.getId());
        entity.setHttpMethod(method);
        entity.setUriPattern(uri);
        entity.setServiceCode(serviceCode);
        entity.setMatchType(request.getMatchType() == null ? PermissionMatchType.EXACT : request.getMatchType());
        entity.setStatus(request.getStatus() == null ? RecordStatus.ACTIVE : request.getStatus());
        entity.setPriority(request.getPriority() == null ? 0 : request.getPriority());
        entity.setIsAllow(Boolean.TRUE.equals(request.getIsAllow()));
        entity.setDescription(trimNullable(request.getDescription()));
        return toResponse(permissionApiRepository.save(entity));
    }

    @Override
    @Transactional
    public PermissionApiResponse update(String permissionApiId, PermissionApiUpdateRequest request) {
        PermissionApi entity = permissionApiRepository.findById(permissionApiId)
                .orElseThrow(() -> new BusinessException(MessageCode.PERMISSION_API_NOT_FOUND, HttpStatus.NOT_FOUND));
        String permissionId = request.getPermissionId() == null ? entity.getPermissionId() : request.getPermissionId().trim();
        Permission permission = getPermissionOrThrow(permissionId);
        validatePermissionActive(permission);
        String method = request.getHttpMethod() == null ? entity.getHttpMethod() : normalizeMethod(request.getHttpMethod());
        String uri = request.getUriPattern() == null ? entity.getUriPattern() : normalizeUri(request.getUriPattern());
        String serviceCode = request.getServiceCode() == null ? entity.getServiceCode() : normalizeServiceCode(request.getServiceCode());
        if (permissionApiRepository.existsByPermissionIdAndHttpMethodIgnoreCaseAndUriPatternAndServiceCodeIgnoreCaseAndIdNot(permission.getId(), method, uri, serviceCode, permissionApiId)) {
            throw new BusinessException(MessageCode.PERMISSION_API_EXISTS, HttpStatus.CONFLICT);
        }

        permissionApiMapper.updateEntity(request, entity);
        entity.setPermissionId(permission.getId());
        entity.setHttpMethod(method);
        entity.setUriPattern(uri);
        entity.setServiceCode(serviceCode);
        entity.setMatchType(request.getMatchType() == null ? entity.getMatchType() : request.getMatchType());
        entity.setStatus(request.getStatus() == null ? entity.getStatus() : request.getStatus());
        entity.setPriority(request.getPriority() == null ? entity.getPriority() : request.getPriority());
        entity.setIsAllow(request.getIsAllow() == null ? entity.getIsAllow() : request.getIsAllow());
        entity.setDescription(request.getDescription() == null ? entity.getDescription() : trimNullable(request.getDescription()));
        return toResponse(permissionApiRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionApiResponse getById(String permissionApiId) {
        PermissionApi entity = permissionApiRepository.findById(permissionApiId)
                .orElseThrow(() -> new BusinessException(MessageCode.PERMISSION_API_NOT_FOUND, HttpStatus.NOT_FOUND));
        return toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PermissionApiResponse> search(PermissionApiSearchRequest request, Pageable pageable) {
        Pageable resolved = resolvePageable(request, pageable);
        Page<PermissionApi> page = permissionApiRepository.findAll(PermissionApiSpecification.bySearchRequest(request), resolved);
        return PageResponse.<PermissionApiResponse>builder()
                .items(page.getContent().stream().map(this::toResponse).toList())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    @Override
    @Transactional
    public void delete(String permissionApiId) {
        PermissionApi entity = permissionApiRepository.findById(permissionApiId)
                .orElseThrow(() -> new BusinessException(MessageCode.PERMISSION_API_NOT_FOUND, HttpStatus.NOT_FOUND));
        permissionApiRepository.delete(entity);
    }

    private PermissionApiResponse toResponse(PermissionApi entity) {
        PermissionApiResponse response = permissionApiMapper.toResponse(entity);
        permissionRepository.findById(entity.getPermissionId()).ifPresent(permission -> {
            response.setPermissionCode(permission.getCode());
            response.setPermissionName(permission.getName());
        });
        return response;
    }

    private Permission getPermissionOrThrow(String permissionId) {
        return permissionRepository.findById(permissionId)
                .orElseThrow(() -> new BusinessException(MessageCode.PERMISSION_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private void validatePermissionActive(Permission permission) {
        if (permission.getStatus() != RecordStatus.ACTIVE) {
            throw new BusinessException(MessageCode.PERMISSION_STATUS_INVALID, HttpStatus.BAD_REQUEST);
        }
    }

    private Pageable resolvePageable(PermissionApiSearchRequest request, Pageable pageable) {
        if (pageable != null && pageable.isPaged()) {
            return pageable;
        }
        String sortField = request != null && StringUtils.hasText(request.getSort()) ? request.getSort() : "createdAt";
        Sort.Direction direction = request != null && "ASC".equalsIgnoreCase(request.getOrder()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        int page = request != null && request.getPage() != null && request.getPage() > 0 ? request.getPage() - 1 : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }

    private String normalizeMethod(String method) {
        if (!StringUtils.hasText(method)) {
            throw new BusinessException(MessageCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        }
        return method.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeUri(String uri) {
        if (!StringUtils.hasText(uri)) {
            throw new BusinessException(MessageCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        }
        String normalized = uri.trim();
        int queryIdx = normalized.indexOf('?');
        if (queryIdx >= 0) {
            normalized = normalized.substring(0, queryIdx);
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(MessageCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        }
        return normalized;
    }

    private String normalizeServiceCode(String serviceCode) {
        String source = StringUtils.hasText(serviceCode) ? serviceCode.trim() : appSettingProperty.getArtifact();
        if (!StringUtils.hasText(source)) {
            source = "LEDGER_SERVICE";
        }
        return source.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    private String trimNullable(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}