package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.config.properties.AppSettingProperty;
import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.context.RequestContext;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.model.dto.response.AuthorizedMenuListResponse;
import com.ledger.ledgerservice.model.dto.response.AuthorizedMenuResponse;
import com.ledger.ledgerservice.model.entity.Group;
import com.ledger.ledgerservice.model.entity.Menu;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.entity.UserGroup;
import com.ledger.ledgerservice.model.enums.MenuType;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.model.enums.UserStatus;
import com.ledger.ledgerservice.repository.GroupRepository;
import com.ledger.ledgerservice.repository.MenuRepository;
import com.ledger.ledgerservice.repository.UserGroupRepository;
import com.ledger.ledgerservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccessControlQueryService {
    private final MenuPermissionService menuPermissionService;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final GroupRepository groupRepository;
    private final AppSettingProperty appSettingProperty;

    @Transactional(readOnly = true)
    public AuthorizedMenuListResponse getAuthorizedMenus() {
        RequestContext ctx = RequestContextHolder.get();
        if (ctx == null || !StringUtils.hasText(ctx.getUsername()) || !StringUtils.hasText(ctx.getUserId())) {
            throw new BusinessException(MessageCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        List<AuthorizedMenuResponse> menus = isSuperAdmin(ctx)
                ? getFullMenuTree()
                : menuPermissionService.getAuthorizedMenus(ctx.getUserId());
        return AuthorizedMenuListResponse.builder()
                .userId(ctx.getUserId())
                .username(ctx.getUsername())
                .menus(menus)
                .build();
    }

    private boolean isSuperAdmin(RequestContext ctx) {
        if (ctx.getUsername().equalsIgnoreCase(appSettingProperty.getSuperUser())) {
            return true;
        }
        User user = userRepository.findById(ctx.getUserId()).orElse(null);
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            return false;
        }
        List<UserGroup> userGroups = userGroupRepository.findActiveByUserIdAt(ctx.getUserId(), RecordStatus.ACTIVE, LocalDateTime.now());
        for (UserGroup userGroup : userGroups) {
            Group group = groupRepository.findById(userGroup.getGroupId()).orElse(null);
            if (group != null && group.getStatus() == RecordStatus.ACTIVE && Boolean.TRUE.equals(group.getIsSuperAdmin())) {
                return true;
            }
        }
        return false;
    }

    private List<AuthorizedMenuResponse> getFullMenuTree() {
        List<Menu> menus = menuRepository.findByStatusAndVisibleTrueOrderByOffsetAscCodeAsc(RecordStatus.ACTIVE).stream()
                .filter(menu -> menu.getMenuType() != MenuType.BUTTON)
                .toList();
        Map<String, AuthorizedMenuResponse> nodeById = new LinkedHashMap<>();
        for (Menu menu : menus) {
            nodeById.put(menu.getId(), AuthorizedMenuResponse.builder()
                    .id(menu.getId())
                    .code(menu.getCode())
                    .name(menu.getName())
                    .path(menu.getPath())
                    .icon(menu.getIcon())
                    .parentId(menu.getParentId())
                    .offset(menu.getOffset())
                    .permissions(List.of())
                    .actions(List.of())
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

    private void sortNodes(List<AuthorizedMenuResponse> nodes) {
        nodes.sort(Comparator.comparing(AuthorizedMenuResponse::getOffset, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(AuthorizedMenuResponse::getCode, Comparator.nullsLast(String::compareToIgnoreCase)));
        for (AuthorizedMenuResponse node : nodes) {
            sortNodes(node.getChildren());
        }
    }
}
