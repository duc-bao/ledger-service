package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.request.MenuPermissionCreateRequest;
import com.ledger.ledgerservice.model.dto.response.AuthorizedMenuResponse;
import com.ledger.ledgerservice.model.dto.response.MenuActionResponse;
import com.ledger.ledgerservice.model.dto.response.MenuPermissionResponse;
import com.ledger.ledgerservice.model.entity.Menu;
import com.ledger.ledgerservice.model.entity.MenuPermission;
import com.ledger.ledgerservice.model.entity.Permission;
import com.ledger.ledgerservice.model.enums.MenuType;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.model.mapper.MenuPermissionMapper;
import com.ledger.ledgerservice.model.security.JwtUserPrincipal;
import com.ledger.ledgerservice.repository.DepartmentUserRepository;
import com.ledger.ledgerservice.repository.MenuPermissionRepository;
import com.ledger.ledgerservice.repository.MenuRepository;
import com.ledger.ledgerservice.repository.PermissionRepository;
import com.ledger.ledgerservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuPermissionServiceImpl implements MenuPermissionService {
    private final MenuPermissionRepository menuPermissionRepository;
    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionResolutionService permissionResolutionService;
    private final DepartmentUserRepository departmentUserRepository;
    private final UserRepository userRepository;
    private final MenuPermissionMapper menuPermissionMapper;

    @Override
    @Transactional
    public MenuPermissionResponse assign(MenuPermissionCreateRequest request) {
        Menu menu = menuRepository.findByIdAndStatus(request.getMenuId(), RecordStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(MessageCode.MENU_NOT_FOUND, HttpStatus.NOT_FOUND));
        Permission permission = permissionRepository.findByIdAndStatus(request.getPermissionId(), RecordStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(MessageCode.PERMISSION_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (menuPermissionRepository.existsByMenuIdAndPermissionId(menu.getId(), permission.getId())) {
            throw new BusinessException(MessageCode.MENU_PERMISSION_EXISTS, HttpStatus.CONFLICT);
        }

        MenuPermission entity = menuPermissionRepository.save(MenuPermission.builder()
                .menuId(menu.getId())
                .permissionId(permission.getId())
                .displayAction(trimNullable(request.getDisplayAction()))
                .status(RecordStatus.ACTIVE)
                .build());
        return toResponse(entity, menu, permission);
    }

    @Override
    @Transactional
    public void revoke(String menuPermissionId) {
        MenuPermission entity = menuPermissionRepository.findById(menuPermissionId)
                .orElseThrow(() -> new BusinessException(MessageCode.MENU_PERMISSION_NOT_FOUND, HttpStatus.NOT_FOUND));
        menuPermissionRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuPermissionResponse> getByMenu(String menuId) {
        Menu menu = menuRepository.findByIdAndStatus(menuId, RecordStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(MessageCode.MENU_NOT_FOUND, HttpStatus.NOT_FOUND));
        List<MenuPermission> mappings = menuPermissionRepository.findByMenuIdAndStatus(menuId, RecordStatus.ACTIVE);
        Map<String, Permission> permissionById = permissionRepository.findAllByIdIn(mappings.stream().map(MenuPermission::getPermissionId).toList()).stream()
                .collect(Collectors.toMap(Permission::getId, permission -> permission));
        return mappings.stream().map(mapping -> toResponse(mapping, menu, permissionById.get(mapping.getPermissionId()))).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthorizedMenuResponse> getAuthorizedMenusForCurrentUser() {
        String userId = getCurrentUserId();
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException(MessageCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        return getAuthorizedMenus(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthorizedMenuResponse> getAuthorizedMenus(String userId) {
        userRepository.findById(userId).orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        Set<String> permissionCodes = new LinkedHashSet<>(permissionResolutionService.getGlobalPermissions(userId));
        for (String departmentId : departmentUserRepository.findActiveDepartmentIdsByUserId(userId)) {
            permissionCodes.addAll(permissionResolutionService.getDepartmentPermissions(userId, departmentId));
        }
        if (permissionCodes.isEmpty()) {
            return List.of();
        }

        List<Permission> permissions = permissionRepository.findByCodeIn(permissionCodes);
        Map<String, Permission> permissionById = permissions.stream().collect(Collectors.toMap(Permission::getId, permission -> permission));
        List<MenuPermission> activeMappings = menuPermissionRepository.findByPermissionIdIn(permissionById.keySet()).stream()
                .filter(mapping -> mapping.getStatus() == RecordStatus.ACTIVE)
                .toList();
        if (activeMappings.isEmpty()) {
            return List.of();
        }

        List<Menu> allMenus = menuRepository.findByStatusAndVisibleTrueOrderByOffsetAscCodeAsc(RecordStatus.ACTIVE);
        Map<String, Menu> allMenusById = allMenus.stream().collect(Collectors.toMap(Menu::getId, menu -> menu));
        Map<String, LinkedHashSet<String>> permissionsByMenuId = new HashMap<>();
        Map<String, LinkedHashMap<String, String>> actionsByMenuId = new HashMap<>();
        Set<String> includedMenuIds = new LinkedHashSet<>();

        for (MenuPermission mapping : activeMappings) {
            Menu menu = allMenusById.get(mapping.getMenuId());
            Permission permission = permissionById.get(mapping.getPermissionId());
            if (menu == null || permission == null) {
                continue;
            }
            includedMenuIds.add(menu.getId());
            permissionsByMenuId.computeIfAbsent(menu.getId(), ignored -> new LinkedHashSet<>()).add(permission.getCode());
            String actionCode = StringUtils.hasText(mapping.getDisplayAction()) ? mapping.getDisplayAction().trim().toUpperCase() : permission.getActionCode();
            actionsByMenuId.computeIfAbsent(menu.getId(), ignored -> new LinkedHashMap<>()).put(actionCode, permission.getCode());
            includeParents(menu, allMenusById, includedMenuIds);
        }

        Map<String, AuthorizedMenuResponse> nodeById = new LinkedHashMap<>();
        for (String menuId : includedMenuIds) {
            Menu menu = allMenusById.get(menuId);
            if (menu == null) {
                continue;
            }
            nodeById.put(menuId, AuthorizedMenuResponse.builder()
                    .id(menu.getId())
                    .code(menu.getCode())
                    .name(menu.getName())
                    .path(menu.getPath())
                    .icon(menu.getIcon())
                    .parentId(menu.getParentId())
                    .offset(menu.getOffset())
                    .permissions(new ArrayList<>(permissionsByMenuId.getOrDefault(menuId, new LinkedHashSet<>())))
                    .actions(actionsByMenuId.getOrDefault(menuId, new LinkedHashMap<>()).entrySet().stream()
                            .map(entry -> MenuActionResponse.builder().code(entry.getKey()).permissionCode(entry.getValue()).build())
                            .toList())
                    .children(new ArrayList<>())
                    .build());
        }

        List<AuthorizedMenuResponse> roots = new ArrayList<>();
        for (AuthorizedMenuResponse node : nodeById.values()) {
            if (StringUtils.hasText(node.getParentId()) && nodeById.containsKey(node.getParentId())) {
                nodeById.get(node.getParentId()).getChildren().add(node);
            } else {
                roots.add(node);
            }
        }
        sortNodes(roots);
        return roots;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
            return jwtUserPrincipal.userId();
        }
        return null;
    }

    private void includeParents(Menu menu, Map<String, Menu> allMenusById, Set<String> includedMenuIds) {
        String parentId = menu.getParentId();
        while (StringUtils.hasText(parentId)) {
            Menu parent = allMenusById.get(parentId);
            if (parent == null || parent.getMenuType() == MenuType.BUTTON) {
                break;
            }
            includedMenuIds.add(parent.getId());
            parentId = parent.getParentId();
        }
    }

    private void sortNodes(List<AuthorizedMenuResponse> nodes) {
        nodes.sort(Comparator.comparing(AuthorizedMenuResponse::getOffset, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(AuthorizedMenuResponse::getCode, Comparator.nullsLast(String::compareToIgnoreCase)));
        for (AuthorizedMenuResponse node : nodes) {
            sortNodes(node.getChildren());
        }
    }

    private MenuPermissionResponse toResponse(MenuPermission entity, Menu menu, Permission permission) {
        MenuPermissionResponse response = menuPermissionMapper.toResponse(entity);
        response.setMenuCode(menu.getCode());
        response.setMenuName(menu.getName());
        if (permission != null) {
            response.setPermissionCode(permission.getCode());
            response.setPermissionName(permission.getName());
        }
        return response;
    }

    private String trimNullable(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
