package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.request.AssignUserRoleRequest;
import com.ledger.ledgerservice.model.dto.request.AdminCreateUserRequest;
import com.ledger.ledgerservice.model.dto.request.CreateRoleWithPermissionsRequest;
import com.ledger.ledgerservice.model.dto.request.MenuActionRequest;
import com.ledger.ledgerservice.model.dto.request.UpsertGroupPermissionsRequest;
import com.ledger.ledgerservice.model.dto.response.AdminPermissionActionResponse;
import com.ledger.ledgerservice.model.dto.response.AdminCreateUserResponse;
import com.ledger.ledgerservice.model.dto.response.AdminPermissionDetailResponse;
import com.ledger.ledgerservice.model.dto.response.AdminPermissionListResponse;
import com.ledger.ledgerservice.model.dto.response.AdminPermissionMenuMatrixResponse;
import com.ledger.ledgerservice.model.dto.response.AdminPermissionSummaryResponse;
import com.ledger.ledgerservice.model.dto.response.EffectivePermissionResponse;
import com.ledger.ledgerservice.model.dto.response.RoleWithPermissionsResponse;
import com.ledger.ledgerservice.model.dto.response.UserRoleResponse;
import com.ledger.ledgerservice.model.entity.Group;
import com.ledger.ledgerservice.model.entity.Menu;
import com.ledger.ledgerservice.model.entity.Permission;
import com.ledger.ledgerservice.model.entity.UserGroup;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.UserStatus;
import com.ledger.ledgerservice.repository.GroupRepository;
import com.ledger.ledgerservice.repository.MenuRepository;
import com.ledger.ledgerservice.repository.PermissionRepository;
import com.ledger.ledgerservice.repository.UserGroupRepository;
import com.ledger.ledgerservice.repository.UserRepository;
import com.ledger.ledgerservice.service.menu.MenuLocalizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccessControlAdminService {
    private static final String DISCRIMINATOR_GROUP = "GROUP";

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MenuRepository menuRepository;
    private final UserGroupRepository userGroupRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionService permissionService;
    private final MenuLocalizationService menuLocalizationService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserRoleResponse assignRoleForUser(AssignUserRoleRequest request) {
        String userId = request.getUserId();
        String groupId = request.getGroupId();

        if (!StringUtils.hasText(userId)) {
            throw new BusinessException(MessageCode.USER_ID_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (!StringUtils.hasText(groupId)) {
            throw new BusinessException(MessageCode.GROUP_ID_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        if (!userRepository.existsById(userId)) {
            throw new BusinessException(MessageCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(MessageCode.GROUP_NOT_FOUND, HttpStatus.NOT_FOUND));

        List<UserGroup> currentMappings = userGroupRepository.findByUserId(userId);
        if (currentMappings.stream().anyMatch(ug -> groupId.equals(ug.getGroupId()))) {
            return UserRoleResponse.builder().userId(userId).groupId(groupId).build();
        }

        userGroupRepository.deleteByUserId(userId);
        userGroupRepository.save(UserGroup.builder().userId(userId).groupId(group.getId()).build());

        permissionService.evictUsersAuthoritiesCacheAfterCommit(Set.of(userId));
        return UserRoleResponse.builder().userId(userId).groupId(group.getId()).build();
    }

    @Transactional
    public void upsertGroupPermissions(UpsertGroupPermissionsRequest request) {
        String groupId = request.getGroupId();
        if (!StringUtils.hasText(groupId)) {
            throw new BusinessException(MessageCode.GROUP_ID_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (!groupRepository.existsById(groupId)) {
            throw new BusinessException(MessageCode.GROUP_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        Set<String> menuIds = request.getMenuActions().stream()
                .map(MenuActionRequest::getMenuId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, Menu> menuMap = menuRepository.findAllById(menuIds).stream()
                .collect(Collectors.toMap(Menu::getId, m -> m));
        if (menuMap.size() != menuIds.size()) {
            throw new BusinessException(MessageCode.MENU_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        List<Permission> newPermissions = new ArrayList<>();
        for (MenuActionRequest menuAction : request.getMenuActions()) {
            Set<String> normalizedActions = menuAction.getActions().stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .collect(Collectors.toCollection(HashSet::new));
            if (normalizedActions.isEmpty()) {
                continue;
            }

            newPermissions.add(Permission.builder()
                    .groupId(groupId)
                    .menuId(menuAction.getMenuId())
                    .discriminator(DISCRIMINATOR_GROUP)
                    .actions(new ArrayList<>(normalizedActions))
                    .build());
        }

        permissionRepository.deleteByGroupId(groupId);
        if (!newPermissions.isEmpty()) {
            permissionRepository.saveAll(newPermissions);
        }

        Set<String> affectedUsers = userGroupRepository.findByGroupId(groupId).stream()
                .map(UserGroup::getUserId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        permissionService.evictUsersAuthoritiesCacheAfterCommit(affectedUsers);
    }

    @Transactional(readOnly = true)
    public EffectivePermissionResponse getEffectivePermissions(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(MessageCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        return EffectivePermissionResponse.builder()
                .userId(userId)
                .authorities(permissionService.getAuthorities(userId, null))
                .build();
    }

    @Transactional(readOnly = true)
    public AdminPermissionListResponse getPermissionList() {
        List<Group> groups = groupRepository.findAll();
        if (groups.isEmpty()) {
            return AdminPermissionListResponse.builder().items(List.of()).build();
        }

        Set<String> groupIds = groups.stream()
                .map(Group::getId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        List<Permission> permissions = permissionRepository.findByGroupIdIn(groupIds);

        Map<String, List<Permission>> permissionsByGroupId = permissions.stream()
                .filter(permission -> StringUtils.hasText(permission.getGroupId()))
                .collect(Collectors.groupingBy(Permission::getGroupId));

        List<AdminPermissionSummaryResponse> items = groups.stream()
                .sorted(Comparator.comparing(Group::getCode, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(group -> {
                    List<Permission> groupPermissions = permissionsByGroupId.getOrDefault(group.getId(), List.of());
                    int totalActions = groupPermissions.stream()
                            .map(Permission::getActions)
                            .filter(actions -> actions != null && !actions.isEmpty())
                            .mapToInt(List::size)
                            .sum();
                    return AdminPermissionSummaryResponse.builder()
                            .groupId(group.getId())
                            .groupCode(group.getCode())
                            .groupName(group.getName())
                            .isDefault(group.getIsDefault())
                            .isSuperAdmin(group.getIsSuperAdmin())
                            .totalMenusAssigned(groupPermissions.size())
                            .totalActionsAssigned(totalActions)
                            .updatedAt(group.getUpdatedAt())
                            .build();
                })
                .toList();

        return AdminPermissionListResponse.builder().items(items).build();
    }

    @Transactional(readOnly = true)
    public AdminPermissionDetailResponse getGroupPermissionDetail(String groupId) {
        if (!StringUtils.hasText(groupId)) {
            throw new BusinessException(MessageCode.GROUP_ID_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(MessageCode.GROUP_NOT_FOUND, HttpStatus.NOT_FOUND));

        List<Permission> groupPermissions = permissionRepository.findByGroupId(groupId);
        Map<String, Set<String>> selectedActionsByMenuId = groupPermissions.stream()
                .filter(permission -> StringUtils.hasText(permission.getMenuId()))
                .collect(Collectors.toMap(
                        Permission::getMenuId,
                        permission -> permission.getActions() == null ? Set.of() : permission.getActions().stream()
                                .filter(StringUtils::hasText)
                                .map(String::trim)
                                .map(String::toUpperCase)
                                .collect(Collectors.toCollection(LinkedHashSet::new)),
                        (left, right) -> {
                            Set<String> merged = new LinkedHashSet<>(left);
                            merged.addAll(right);
                            return merged;
                        }
                ));

        List<Menu> menus = menuRepository.findAllByOrderByOffsetAscCodeAsc();
        List<AdminPermissionMenuMatrixResponse> matrix = menus.stream()
                .map(menu -> {
                    Set<String> selectedActions = selectedActionsByMenuId.getOrDefault(menu.getId(), Set.of());
                    List<String> availableActions = menu.getActions() == null ? List.of() : menu.getActions();

                    List<AdminPermissionActionResponse> actions = availableActions.stream()
                            .filter(StringUtils::hasText)
                            .map(String::trim)
                            .map(String::toUpperCase)
                            .distinct()
                            .map(action -> AdminPermissionActionResponse.builder()
                                    .action(action)
                                    .selected(selectedActions.contains(action))
                                    .build())
                            .toList();

                    return AdminPermissionMenuMatrixResponse.builder()
                            .menuId(menu.getId())
                            .menuCode(menu.getCode())
                            .displayName(menuLocalizationService.getDisplayName(menu.getCode()))
                            .parentId(menu.getParentId())
                            .path(menu.getPath())
                            .icon(menu.getIcon())
                            .offset(menu.getOffset())
                            .actions(actions)
                            .build();
                })
                .toList();

        return AdminPermissionDetailResponse.builder()
                .groupId(group.getId())
                .groupCode(group.getCode())
                .groupName(group.getName())
                .hasPermissions(!groupPermissions.isEmpty())
                .permissionMatrix(matrix)
                .build();
    }

    @Transactional
    public RoleWithPermissionsResponse createRoleWithPermissions(CreateRoleWithPermissionsRequest request) {
        String code = request.getCode() == null ? null : request.getCode().trim();
        String name = request.getName() == null ? null : request.getName().trim();
        String description = request.getDescription() == null ? null : request.getDescription().trim();

        if (groupRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException(MessageCode.GROUP_CODE_EXISTS, HttpStatus.CONFLICT);
        }
        if (groupRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException(MessageCode.GROUP_NAME_EXISTS, HttpStatus.CONFLICT);
        }

        Set<String> menuIds = request.getMenuActions().stream()
                .map(MenuActionRequest::getMenuId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, Menu> menuMap = menuRepository.findAllById(menuIds).stream()
                .collect(Collectors.toMap(Menu::getId, m -> m));
        if (menuMap.size() != menuIds.size()) {
            throw new BusinessException(MessageCode.MENU_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        Group group = groupRepository.save(Group.builder()
                .code(code)
                .name(name)
                .description(description)
                .isDefault(false)
                .isSuperAdmin(false)
                .build());

        List<Permission> permissions = new ArrayList<>();
        int totalActionsAssigned = 0;
        for (MenuActionRequest menuAction : request.getMenuActions()) {
            Set<String> normalizedActions = menuAction.getActions().stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (normalizedActions.isEmpty()) {
                continue;
            }
            totalActionsAssigned += normalizedActions.size();
            permissions.add(Permission.builder()
                    .groupId(group.getId())
                    .menuId(menuAction.getMenuId())
                    .discriminator(DISCRIMINATOR_GROUP)
                    .actions(new ArrayList<>(normalizedActions))
                    .build());
        }
        if (!permissions.isEmpty()) {
            permissionRepository.saveAll(permissions);
        }

        return RoleWithPermissionsResponse.builder()
                .groupId(group.getId())
                .code(group.getCode())
                .name(group.getName())
                .totalMenusAssigned(permissions.size())
                .totalActionsAssigned(totalActionsAssigned)
                .build();
    }

    @Transactional
    public AdminCreateUserResponse createUserByAdmin(AdminCreateUserRequest request) {
        String username = request.getUsername() == null ? null : request.getUsername().trim();
        String email = request.getEmail() == null ? null : request.getEmail().trim();
        String phone = request.getPhone() == null ? null : request.getPhone().trim();
        String fullName = request.getFullName() == null ? null : request.getFullName().trim();
        String userType = request.getUserType() == null ? null : request.getUserType().trim();
        String groupId = request.getGroupId() == null ? null : request.getGroupId().trim();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new BusinessException(MessageCode.USERNAME_EXISTS, HttpStatus.CONFLICT);
        }
        if (StringUtils.hasText(email) && userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException(MessageCode.USER_EMAIL_EXISTS, HttpStatus.CONFLICT);
        }
        if (StringUtils.hasText(phone) && userRepository.existsByPhone(phone)) {
            throw new BusinessException(MessageCode.USER_PHONE_EXISTS, HttpStatus.CONFLICT);
        }

        Group group = null;
        if (StringUtils.hasText(groupId)) {
            group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new BusinessException(MessageCode.GROUP_NOT_FOUND, HttpStatus.NOT_FOUND));
        }

        User user = userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(request.getPassword()))
                .email(StringUtils.hasText(email) ? email : null)
                .phone(StringUtils.hasText(phone) ? phone : null)
                .fullName(StringUtils.hasText(fullName) ? fullName : null)
                .userType(StringUtils.hasText(userType) ? userType : null)
                .requireChange(Boolean.TRUE.equals(request.getRequireChange()))
                .status(UserStatus.ACTIVE)
                .build());

        if (group != null) {
            userGroupRepository.save(UserGroup.builder()
                    .userId(user.getId())
                    .groupId(group.getId())
                    .build());
        }

        return AdminCreateUserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .userType(user.getUserType())
                .requireChange(user.getRequireChange())
                .groupId(group != null ? group.getId() : null)
                .build();
    }
}
