package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.context.RequestContext;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.model.dto.response.AuthorizedMenuListResponse;
import com.ledger.ledgerservice.model.dto.response.AuthorizedMenuResponse;
import com.ledger.ledgerservice.model.entity.Menu;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.repository.MenuRepository;
import com.ledger.ledgerservice.repository.UserRepository;
import com.ledger.ledgerservice.service.menu.MenuLocalizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AccessControlQueryService {

    private final PermissionService permissionService;
    private final MenuRepository menuRepository;
    private final MenuLocalizationService menuLocalizationService;
    private final UserRepository userRepository;

    public AuthorizedMenuListResponse getAuthorizedMenus() {
        // 1) Resolve current user from request context. Stop early if unauthenticated.
        RequestContext ctx = RequestContextHolder.get();
        if (ctx == null || !StringUtils.hasText(ctx.getUsername())) {
            throw new BusinessException(MessageCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String username = ctx.getUsername();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        String userId = user.getId();

        // 2) Read effective authorities from permission cache/service.
        Set<String> authorities = permissionService.getAuthorities(userId, username);
        // 3) Parse authorities in one pass: MENU_CODE:ACTION -> map(menuCode -> actions).
        //    This avoids multiple stream transformations and reduces allocations under high load.
        Map<String, Set<String>> actionsByMenuCode = new HashMap<>();
        for (String authority : authorities) {
            if (!StringUtils.hasText(authority)) {
                continue;
            }
            String normalized = authority.toUpperCase();
            int separatorIdx = normalized.indexOf(':');
            if (separatorIdx <= 0 || separatorIdx == normalized.length() - 1) {
                continue;
            }

            String menuCode = normalized.substring(0, separatorIdx);
            String action = normalized.substring(separatorIdx + 1);
            actionsByMenuCode.computeIfAbsent(menuCode, ignored -> new LinkedHashSet<>()).add(action);
        }

        if (actionsByMenuCode.isEmpty()) {
            return AuthorizedMenuListResponse.builder()
                    .userId(userId)
                    .username(username)
                    .menus(List.of())
                    .build();
        }

        // 4) Query only menus that appear in permission map (instead of scanning all menus).
        List<Menu> menus = menuRepository.findByCodeInOrderByOffset(actionsByMenuCode.keySet());

        // 5) Build response and keep only valid actions that exist on each menu.
        List<AuthorizedMenuResponse> items = new ArrayList<>();
        for (Menu menu : menus) {
            if (!StringUtils.hasText(menu.getCode())) {
                continue;
            }
            Set<String> allowedActions = actionsByMenuCode.get(menu.getCode().toUpperCase());
            if (allowedActions == null || allowedActions.isEmpty()) {
                continue;
            }

            List<String> availableActions = menu.getActions() == null ? List.of() : menu.getActions();
            List<String> normalizedAvailableActions = new ArrayList<>(availableActions.size());
            Set<String> distinctAvailable = new HashSet<>();
            for (String value : availableActions) {
                if (!StringUtils.hasText(value)) {
                    continue;
                }
                String action = value.toUpperCase();
                if (distinctAvailable.add(action)) {
                    normalizedAvailableActions.add(action);
                }
            }

            List<String> effectiveActions = new ArrayList<>();
            if (allowedActions.contains("*")) {
                effectiveActions = normalizedAvailableActions;
            } else {
                for (String action : normalizedAvailableActions) {
                    if (allowedActions.contains(action)) {
                        effectiveActions.add(action);
                    }
                }
            }

            if (effectiveActions.isEmpty()) {
                continue;
            }

            items.add(AuthorizedMenuResponse.builder()
                    .menuId(menu.getId())
                    .code(menu.getCode())
                    .displayName(menuLocalizationService.getDisplayName(menu.getCode()))
                    .path(menu.getPath())
                    .icon(menu.getIcon())
                    .parentId(menu.getParentId())
                    .offset(menu.getOffset())
                    .actions(effectiveActions)
                    .build());
        }

        // 6) Return localized menu payload for frontend to render UI authorization.
        return AuthorizedMenuListResponse.builder()
                .userId(userId)
                .username(username)
                .menus(items)
                .build();
    }
}
