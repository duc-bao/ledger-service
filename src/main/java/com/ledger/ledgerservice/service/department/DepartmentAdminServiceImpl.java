package com.ledger.ledgerservice.service.department;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.model.dto.request.CreateDepartmentRequest;
import com.ledger.ledgerservice.model.dto.request.DepartmentSearchRequest;
import com.ledger.ledgerservice.model.dto.request.UpdateDepartmentRequest;
import com.ledger.ledgerservice.model.dto.response.DepartmentResponse;
import com.ledger.ledgerservice.model.dto.response.DepartmentTreeResponse;
import com.ledger.ledgerservice.model.entity.DepartmentEntity;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartment(String departmentId) {
        DepartmentEntity department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException(MessageCode.NOT_FOUND, HttpStatus.NOT_FOUND));
        return toResponse(department);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentResponse> searchDepartments(DepartmentSearchRequest request) {
        int requestPage = request == null || request.getPage() == null ? 1 : request.getPage();
        int page = requestPage <= 0 ? 0 : requestPage - 1;
        int size = request == null || request.getSize() == null || request.getSize() <= 0 ? 20 : Math.min(request.getSize(), 200);
        String keyword = request == null || !StringUtils.hasText(request.getKeyword()) ? "" : request.getKeyword().trim().toLowerCase(Locale.ROOT);
        String status = request == null || !StringUtils.hasText(request.getStatus()) ? null : request.getStatus().trim().toUpperCase(Locale.ROOT);
        Boolean isActive = request == null ? null : request.getIsActive();
        return departmentRepository.searchDepartments(keyword, status, isActive, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentTreeResponse> getDepartmentTree() {
        List<DepartmentEntity> departments = departmentRepository.findByIsActiveTrueOrderByTreeLevelAscSortOrderAscCodeAsc();
        if (departments.isEmpty()) {
            return List.of();
        }

        Map<String, List<DepartmentTreeResponse>> childrenByParentId = new HashMap<>();
        List<DepartmentTreeResponse> allNodes = new ArrayList<>();
        Set<String> departmentIds = new HashSet<>();

        for (DepartmentEntity dept : departments) {
            departmentIds.add(dept.getId());
            DepartmentTreeResponse node = toTreeResponse(dept);
            allNodes.add(node);
            if (StringUtils.hasText(dept.getParentId())) {
                childrenByParentId.computeIfAbsent(dept.getParentId(), ignored -> new ArrayList<>()).add(node);
            }
        }

        for (List<DepartmentTreeResponse> children : childrenByParentId.values()) {
            children.sort(departmentTreeComparator());
        }

        List<DepartmentTreeResponse> roots = new ArrayList<>();
        for (DepartmentTreeResponse node : allNodes) {
            if (!StringUtils.hasText(node.getParentId()) || !departmentIds.contains(node.getParentId())) {
                roots.add(node);
            }
        }
        roots.sort(departmentTreeComparator());

        for (DepartmentTreeResponse root : roots) {
            attachChildren(root, childrenByParentId);
        }

        return roots;
    }

    private void attachChildren(DepartmentTreeResponse node, Map<String, List<DepartmentTreeResponse>> childrenByParentId) {
        List<DepartmentTreeResponse> children = childrenByParentId.get(node.getId());
        if (children == null || children.isEmpty()) {
            node.setChildren(new ArrayList<>());
            return;
        }

        for (DepartmentTreeResponse child : children) {
            attachChildren(child, childrenByParentId);
        }
        node.setChildren(children);
    }

    private Comparator<DepartmentTreeResponse> departmentTreeComparator() {
        return Comparator.comparing(DepartmentTreeResponse::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(DepartmentTreeResponse::getCode, Comparator.nullsLast(String::compareToIgnoreCase));
    }

    private DepartmentTreeResponse toTreeResponse(DepartmentEntity entity) {
        return DepartmentTreeResponse.builder()
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
                .children(new ArrayList<>())
                .build();
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
