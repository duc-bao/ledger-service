package com.ledger.ledgerservice.config.filter;

import com.ledger.ledgerservice.config.properties.AppSettingProperty;
import com.ledger.ledgerservice.model.context.RequestContext;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.model.security.JwtUserPrincipal;
import com.ledger.ledgerservice.service.security.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PermissionFilter implements PermissionEvaluator {
  private static final String FILTER_PATTERN = "[-;,|&]";
  private final AppSettingProperty appSettingProperty;
  private final PermissionService permissionService;

  public PermissionFilter(AppSettingProperty appSettingProperty, PermissionService permissionService) {
    this.appSettingProperty = appSettingProperty;
    this.permissionService = permissionService;
  }

  @Override
  public boolean hasPermission(Authentication auth, Object code, Object action) {
    if ((auth == null) || (code == null) || !(action instanceof String)) return false;

    return hasPrivilege(auth, code.toString(), action.toString().toUpperCase());
  }

  @Override
  public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
    if ((targetType == null) || !(permission instanceof String)) return false;

    return hasPrivilege(auth, targetType.toUpperCase(), permission.toString().toUpperCase());
  }

  private boolean hasPrivilege(Authentication auth, String codes, String actions) {
    try {
      RequestContext ctx = RequestContextHolder.get();
      if (ctx != null && StringUtils.hasText(ctx.getUsername())
              && ctx.getUsername().equalsIgnoreCase(appSettingProperty.getSuperUser())) {
        return true;
      }

      if (auth == null) {
        return false;
      }
      String userId = resolveUserId(auth);
      String username = resolveUsername(auth);
      if (StringUtils.hasText(username) && username.equalsIgnoreCase(appSettingProperty.getSuperUser())) {
        return true;
      }

      Set<String> acceptActions = Arrays.stream(
        Pattern.compile(FILTER_PATTERN).split(actions.toUpperCase())
      ).map(String::valueOf).collect(Collectors.toSet());
      Set<String> menuCodes = Arrays.stream(
        Pattern.compile(FILTER_PATTERN).split(codes.toUpperCase())
      ).map(String::valueOf).collect(Collectors.toSet());

      Set<String> authorities = permissionService.getAuthorities(userId, username);
      for (String value : authorities) {
        if (!StringUtils.hasText(value)) {
          continue;
        }

        String normalized = value.toUpperCase();
        if ("*:*".equals(normalized) || normalized.endsWith(":*")) {
          return true;
        }

        String[] parts = normalized.split(":", 2);
        if (parts.length != 2) {
          continue;
        }

        if (menuCodes.contains(parts[0]) && (acceptActions.contains(parts[1]) || "*".equals(parts[1]))) {
          return true;
        }
      }

      return false;
    } catch (Exception ex) {
      log.error("Permission check failed", ex);
      return false;
    }
  }

  private String resolveUserId(Authentication auth) {
    Object principal = auth.getPrincipal();
    if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
      return jwtUserPrincipal.userId();
    }
    return null;
  }

  private String resolveUsername(Authentication auth) {
    Object principal = auth.getPrincipal();
    if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
      return jwtUserPrincipal.username();
    }
    return principal != null ? String.valueOf(principal) : null;
  }
}
