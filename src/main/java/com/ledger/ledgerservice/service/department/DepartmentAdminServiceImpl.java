package com.ledger.ledgerservice.service.department;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.model.dto.request.CreateDepartmentRequest;
import com.ledger.ledgerservice.model.dto.request.UpdateDepartmentRequest;
import com.ledger.ledgerservice.model.dto.response.DepartmentResponse;
import com.ledger.ledgerservice.model.entity.DepartmentEntity;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DepartmentAdminServiceImpl implements DepartmentAdminService {
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        String code = trimRequired(request.getCode());
        if (departmentRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException(MessageCode.EXISTED, HttpStatus.CONFLICT);
        }

        DepartmentEntity parent = resolveParent(request.getParentId(), null);
        int sortOrder = request.getSortOrder() != null ? request.getSortOrder() : nextSortOrder(parent != null ? parent.getId() : null);

        DepartmentEntity saved = departmentRepository.save(DepartmentEntity.builder()
                .code(code.toUpperCase(Locale.ROOT))
                .name(trimRequired(request.getName()))
                .shortName(trimNullable(request.getShortName()))
                .parentId(parent != null ? parent.getId() : null)
                .ancestors(buildAncestors(parent))
                .treeLevel(parent != null ? parent.getTreeLevel() + 1 : 0)
                .sortOrder(sortOrder)
                .managerUserId(trimNullable(request.getManagerUserId()))
                .status("ACTIVE")
                .isActive(Boolean.TRUE)
                .createdBy(resolveActor())
                .updatedBy(resolveActor())
                .build());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(String departmentId, UpdateDepartmentRequest request) {
        DepartmentEntity department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException(MessageCode.NOT_FOUND, HttpStatus.NOT_FOUND));
        DepartmentEntity parent = resolveParent(request.getParentId(), departmentId);
        String oldParentId = department.getParentId();

        department.setName(trimRequired(request.getName()));
        department.setShortName(trimNullable(request.getShortName()));
        department.setParentId(parent != null ? parent.getId() : null);
        department.setAncestors(buildAncestors(parent));
        department.setTreeLevel(parent != null ? parent.getTreeLevel() + 1 : 0);
        department.setManagerUserId(trimNullable(request.getManagerUserId()));
        if (request.getSortOrder() != null) {
            department.setSortOrder(request.getSortOrder());
        }
        if (StringUtils.hasText(request.getStatus())) {
            department.setStatus(request.getStatus().trim().toUpperCase(Locale.ROOT));
        }
        if (request.getIsActive() != null) {
            department.setIsActive(request.getIsActive());
        }
        department.setUpdatedAt(LocalDateTime.now());
        department.setUpdatedBy(resolveActor());

        DepartmentEntity saved = departmentRepository.save(department);
        if (!safeEquals(oldParentId, department.getParentId())) {
            updateDescendants(saved);
        }
        return toResponse(saved);
    }

    private DepartmentEntity resolveParent(String parentId, String currentDepartmentId) {
        if (!StringUtils.hasText(parentId)) {
            return null;
        }
        if (parentId.trim().equals(currentDepartmentId)) {
            throw new BusinessException(MessageCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        }
        DepartmentEntity parent = departmentRepository.findById(parentId.trim())
                .orElseThrow(() -> new BusinessException(MessageCode.NOT_FOUND, HttpStatus.NOT_FOUND));
        if (currentDepartmentId != null) {
            validateNoCycle(currentDepartmentId, parent);
        }
        return parent;
    }

    private void validateNoCycle(String departmentId, DepartmentEntity parent) {
        DepartmentEntity current = parent;
        while (current != null) {
            if (departmentId.equals(current.getId())) {
                throw new BusinessException(MessageCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
            }
            current = StringUtils.hasText(current.getParentId()) ? departmentRepository.findById(current.getParentId()).orElse(null) : null;
        }
    }

    private void updateDescendants(DepartmentEntity parent) {
        List<DepartmentEntity> children = departmentRepository.findByParentIdOrderBySortOrderAscCodeAsc(parent.getId());
        for (DepartmentEntity child : children) {
            child.setAncestors(buildAncestors(parent));
            child.setTreeLevel(parent.getTreeLevel() + 1);
            child.setUpdatedAt(LocalDateTime.now());
            child.setUpdatedBy(resolveActor());
            departmentRepository.save(child);
            updateDescendants(child);
        }
    }

    private int nextSortOrder(String parentId) {
        return departmentRepository.findByParentIdOrderBySortOrderAscCodeAsc(parentId).stream()
                .map(DepartmentEntity::getSortOrder)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private String buildAncestors(DepartmentEntity parent) {
        if (parent == null) {
            return null;
        }
        if (!StringUtils.hasText(parent.getAncestors())) {
            return parent.getId();
        }
        return parent.getAncestors() + "," + parent.getId();
    }

    private DepartmentResponse toResponse(DepartmentEntity entity) {
        return DepartmentResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .shortName(entity.getShortName())
                .parentId(entity.getParentId())
                .ancestors(entity.getAncestors())
                .sortOrder(entity.getSortOrder())
                .treeLevel(entity.getTreeLevel())
                .managerUserId(entity.getManagerUserId())
                .status(entity.getStatus())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
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

    private boolean safeEquals(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }
}
