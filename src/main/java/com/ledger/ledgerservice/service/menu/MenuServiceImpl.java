package com.ledger.ledgerservice.service.menu;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.request.MenuCreateRequest;
import com.ledger.ledgerservice.model.dto.request.MenuSearchRequest;
import com.ledger.ledgerservice.model.dto.request.MenuUpdateRequest;
import com.ledger.ledgerservice.model.dto.response.MenuResponse;
import com.ledger.ledgerservice.model.dto.response.PageResponse;
import com.ledger.ledgerservice.model.entity.Menu;
import com.ledger.ledgerservice.model.entity.MenuPermission;
import com.ledger.ledgerservice.model.entity.Permission;
import com.ledger.ledgerservice.model.enums.MenuType;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.model.mapper.MenuMapper;
import com.ledger.ledgerservice.repository.MenuPermissionRepository;
import com.ledger.ledgerservice.repository.MenuRepository;
import com.ledger.ledgerservice.repository.PermissionRepository;
import com.ledger.ledgerservice.repository.specification.MenuSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9_]+$");

    private final MenuRepository menuRepository;
    private final MenuPermissionRepository menuPermissionRepository;
    private final PermissionRepository permissionRepository;
    private final MenuMapper menuMapper;

    @Override
    @Transactional
    public MenuResponse create(MenuCreateRequest request) {
        String normalizedCode = normalizeCode(request.getCode());
        if (menuRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new BusinessException(MessageCode.MENU_CODE_EXISTS, HttpStatus.CONFLICT);
        }

        Menu parent = resolveParent(request.getParentId(), null);
        Menu menu = menuMapper.toEntity(request);
        menu.setCode(normalizedCode);
        menu.setName(trimRequired(request.getName(), MessageCode.NAME_INVALID));
        menu.setPath(trimNullable(request.getPath()));
        menu.setParentId(parent == null ? null : parent.getId());
        menu.setIcon(trimNullable(request.getIcon()));
        menu.setActions(normalizeActions(request.getActions()));
        menu.setOffset(request.getOffset() == null ? 0 : request.getOffset());
        menu.setMenuType(request.getMenuType() == null ? MenuType.MENU : request.getMenuType());
        menu.setComponent(trimNullable(request.getComponent()));
        menu.setVisible(request.getVisible() == null ? Boolean.TRUE : request.getVisible());
        menu.setStatus(RecordStatus.ACTIVE);
        menu.setExternalUrl(trimNullable(request.getExternalUrl()));
        menu.setDescription(trimNullable(request.getDescription()));
        menu.setAncestors(buildAncestors(parent));
        Menu saved = menuRepository.save(menu);

        syncPermissions(saved.getId(), request.getPermissionIds());
        return buildResponse(saved);
    }

    @Override
    @Transactional
    public MenuResponse update(String menuId, MenuUpdateRequest request) {
        Menu menu = getMenuOrThrow(menuId);
        Menu parent = request.getParentId() == null ? resolveExistingParent(menu.getParentId()) : resolveParent(request.getParentId(), menuId);

        menuMapper.updateEntity(request, menu);
        if (request.getName() != null) {
            menu.setName(trimRequired(request.getName(), MessageCode.NAME_INVALID));
        }
        if (request.getPath() != null) {
            menu.setPath(trimNullable(request.getPath()));
        }
        if (request.getParentId() != null) {
            menu.setParentId(parent == null ? null : parent.getId());
            menu.setAncestors(buildAncestors(parent));
        }
        if (request.getIcon() != null) {
            menu.setIcon(trimNullable(request.getIcon()));
        }
        if (request.getActions() != null) {
            menu.setActions(normalizeActions(request.getActions()));
        }
        if (request.getOffset() != null) {
            menu.setOffset(request.getOffset());
        }
        if (request.getMenuType() != null) {
            if (request.getMenuType() == MenuType.BUTTON && menuRepository.existsByParentId(menuId)) {
                throw new BusinessException(MessageCode.MENU_PARENT_INVALID, HttpStatus.BAD_REQUEST);
            }
            menu.setMenuType(request.getMenuType());
        }
        if (request.getComponent() != null) {
            menu.setComponent(trimNullable(request.getComponent()));
        }
        if (request.getVisible() != null) {
            menu.setVisible(request.getVisible());
        }
        if (request.getStatus() != null) {
            menu.setStatus(request.getStatus());
        }
        if (request.getExternalUrl() != null) {
            menu.setExternalUrl(trimNullable(request.getExternalUrl()));
        }
        if (request.getDescription() != null) {
            menu.setDescription(trimNullable(request.getDescription()));
        }

        Menu saved = menuRepository.save(menu);
        if (request.getPermissionIds() != null) {
            syncPermissions(saved.getId(), request.getPermissionIds());
        }
        refreshDescendantAncestors(saved);
        return buildResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuResponse getById(String menuId) {
        return buildResponse(getMenuOrThrow(menuId));
    }

    @Override
    @Transactional(readOnly = true)
    public MenuResponse getByCode(String code) {
        Menu menu = menuRepository.findByCodeIgnoreCase(normalizeCode(code))
                .orElseThrow(() -> new BusinessException(MessageCode.MENU_NOT_FOUND, HttpStatus.NOT_FOUND));
        return buildResponse(menu);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MenuResponse> search(MenuSearchRequest request, Pageable pageable) {
        Pageable resolved = resolvePageable(request, pageable);
        Page<Menu> page = menuRepository.findAll(MenuSpecification.bySearchRequest(request), resolved);
        List<MenuResponse> items = enrichResponses(page.getContent());
        return PageResponse.<MenuResponse>builder()
                .items(items)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuResponse> getTree(RecordStatus status, Boolean visibleOnly) {
        return enrichResponses(menuRepository.findAll().stream()
                .filter(menu -> status == null || menu.getStatus() == status)
                .filter(menu -> visibleOnly == null || !visibleOnly || Boolean.TRUE.equals(menu.getVisible()))
                .sorted(Comparator.comparing(Menu::getOffset).thenComparing(Menu::getCode, String.CASE_INSENSITIVE_ORDER))
                .toList());
    }

    @Override
    @Transactional
    public void changeStatus(String menuId, RecordStatus status) {
        if (status == null) {
            throw new BusinessException(MessageCode.MENU_STATUS_INVALID, HttpStatus.BAD_REQUEST);
        }
        Menu menu = getMenuOrThrow(menuId);
        menu.setStatus(status);
        menuRepository.save(menu);
    }

    @Override
    @Transactional
    public void delete(String menuId) {
        Menu menu = getMenuOrThrow(menuId);
        if (menuRepository.existsByParentId(menuId)) {
            throw new BusinessException(MessageCode.MENU_HAS_CHILDREN, HttpStatus.CONFLICT);
        }
        List<MenuPermission> mappings = menuPermissionRepository.findByMenuId(menuId);
        if (!mappings.isEmpty()) {
            menuPermissionRepository.deleteAll(mappings);
        }
        menuRepository.delete(menu);
    }

    private Menu getMenuOrThrow(String menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(MessageCode.MENU_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private Menu resolveParent(String parentId, String menuId) {
        if (!StringUtils.hasText(parentId)) {
            return null;
        }
        if (parentId.trim().equals(menuId)) {
            throw new BusinessException(MessageCode.MENU_PARENT_INVALID, HttpStatus.BAD_REQUEST);
        }
        Menu parent = getMenuOrThrow(parentId.trim());
        if (parent.getMenuType() == MenuType.BUTTON) {
            throw new BusinessException(MessageCode.MENU_PARENT_INVALID, HttpStatus.BAD_REQUEST);
        }
        if (menuId != null && parent.getAncestors().contains(menuId)) {
            throw new BusinessException(MessageCode.MENU_PARENT_INVALID, HttpStatus.BAD_REQUEST);
        }
        return parent;
    }

    private Menu resolveExistingParent(String parentId) {
        if (!StringUtils.hasText(parentId)) {
            return null;
        }
        return menuRepository.findById(parentId).orElse(null);
    }

    private List<String> buildAncestors(Menu parent) {
        if (parent == null) {
            return new ArrayList<>();
        }
        List<String> ancestors = new ArrayList<>(parent.getAncestors());
        ancestors.add(parent.getId());
        return ancestors;
    }

    private void syncPermissions(String menuId, Collection<String> permissionIds) {
        Set<String> targetPermissionIds = permissionIds == null
                ? Set.of()
                : permissionIds.stream()
                  .filter(StringUtils::hasText)
                  .map(String::trim)
                  .collect(Collectors.toCollection(LinkedHashSet::new));

        List<MenuPermission> existingMappings = menuPermissionRepository.findByMenuId(menuId);
        Map<String, MenuPermission> existingByPermissionId = new LinkedHashMap<>();
        for (MenuPermission mapping : existingMappings) {
            existingByPermissionId.put(mapping.getPermissionId(), mapping);
        }

        Map<String, Permission> permissionsById = targetPermissionIds.isEmpty()
                ? Map.of()
                : permissionRepository.findAllByIdIn(targetPermissionIds).stream()
                  .collect(Collectors.toMap(Permission::getId, permission -> permission));
        if (permissionsById.size() != targetPermissionIds.size()) {
            throw new BusinessException(MessageCode.PERMISSION_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        if (permissionsById.values().stream().anyMatch(permission -> permission.getStatus() != RecordStatus.ACTIVE)) {
            throw new BusinessException(MessageCode.PERMISSION_STATUS_INVALID, HttpStatus.BAD_REQUEST);
        }

        List<MenuPermission> additions = new ArrayList<>();
        for (String permissionId : targetPermissionIds) {
            if (!existingByPermissionId.containsKey(permissionId)) {
                Permission permission = permissionsById.get(permissionId);
                additions.add(MenuPermission.builder()
                        .menuId(menuId)
                        .permissionId(permissionId)
                        .displayAction(permission.getActionCode())
                        .status(RecordStatus.ACTIVE)
                        .build());
            }
        }

        List<MenuPermission> removals = existingMappings.stream()
                .filter(mapping -> !targetPermissionIds.contains(mapping.getPermissionId()))
                .toList();

        if (!removals.isEmpty()) {
            menuPermissionRepository.deleteAll(removals);
        }
        if (!additions.isEmpty()) {
            menuPermissionRepository.saveAll(additions);
        }
    }

    private MenuResponse buildResponse(Menu menu) {
        return enrichResponses(List.of(menu)).getFirst();
    }

    private List<MenuResponse> enrichResponses(List<Menu> menus) {
        List<MenuResponse> responses = menuMapper.toResponses(menus);
        if (responses.isEmpty()) {
            return responses;
        }

        Set<String> parentIds = menus.stream()
                .map(Menu::getParentId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, Menu> parentById = parentIds.isEmpty()
                ? Map.of()
                : menuRepository.findAllById(parentIds).stream()
                  .collect(Collectors.toMap(Menu::getId, parent -> parent));

        Set<String> menuIds = menus.stream().map(Menu::getId).collect(Collectors.toSet());
        List<MenuPermission> mappings = menuPermissionRepository.findByMenuIdIn(menuIds);
        Map<String, List<MenuPermission>> mappingsByMenuId = mappings.stream()
                .collect(Collectors.groupingBy(MenuPermission::getMenuId));

        Set<String> permissionIds = mappings.stream().map(MenuPermission::getPermissionId).collect(Collectors.toSet());
        Map<String, Permission> permissionById = permissionIds.isEmpty()
                ? Map.of()
                : permissionRepository.findAllByIdIn(permissionIds).stream()
                  .collect(Collectors.toMap(Permission::getId, permission -> permission));

        Map<String, MenuResponse> responseById = responses.stream()
                .collect(Collectors.toMap(MenuResponse::getId, response -> response));

        for (Menu menu : menus) {
            MenuResponse response = responseById.get(menu.getId());
            Menu parent = StringUtils.hasText(menu.getParentId()) ? parentById.get(menu.getParentId()) : null;
            if (parent != null) {
                response.setParentCode(parent.getCode());
                response.setParentName(parent.getName());
            }

            List<MenuPermission> menuMappings = mappingsByMenuId.getOrDefault(menu.getId(), List.of());
            List<String> boundPermissionIds = new ArrayList<>();
            List<String> boundPermissionCodes = new ArrayList<>();
            for (MenuPermission mapping : menuMappings) {
                boundPermissionIds.add(mapping.getPermissionId());
                Permission permission = permissionById.get(mapping.getPermissionId());
                if (permission != null) {
                    boundPermissionCodes.add(permission.getCode());
                }
            }
            response.setPermissionIds(boundPermissionIds);
            response.setPermissionCodes(boundPermissionCodes);
        }
        return responses;
    }

    private void refreshDescendantAncestors(Menu menu) {
        List<Menu> descendants = menuRepository.findByAncestorId(menu.getId());
        if (descendants.isEmpty()) {
            return;
        }
        for (Menu descendant : descendants) {
            Menu parent = resolveExistingParent(descendant.getParentId());
            descendant.setAncestors(buildAncestors(parent));
        }
        menuRepository.saveAll(descendants);
    }

    private Pageable resolvePageable(MenuSearchRequest request, Pageable pageable) {
        if (pageable != null && pageable.isPaged()) {
            return pageable;
        }
        String sortField = request != null && StringUtils.hasText(request.getSort()) ? request.getSort() : "offset";
        Sort.Direction direction = request != null && "DESC".equalsIgnoreCase(request.getOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        int page = request != null && request.getPage() != null && request.getPage() > 0 ? request.getPage() - 1 : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        return PageRequest.of(page, size, Sort.by(direction, sortField).and(Sort.by(Sort.Direction.ASC, "code")));
    }

    private String normalizeCode(String code) {
        String normalized = trimRequired(code, MessageCode.CODE_INVALID).toUpperCase();
        if (!CODE_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException(MessageCode.CODE_INVALID, HttpStatus.BAD_REQUEST);
        }
        return normalized;
    }

    private List<String> normalizeActions(List<String> actions) {
        if (actions == null) {
            return new ArrayList<>();
        }
        return actions.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(String::toUpperCase)
                .distinct()
                .toList();
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
