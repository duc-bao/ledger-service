package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.config.properties.AppSettingProperty;
import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.entity.PermissionApi;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.PermissionMatchType;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.model.enums.UserStatus;
import com.ledger.ledgerservice.repository.PermissionApiRepository;
import com.ledger.ledgerservice.repository.PermissionRepository;
import com.ledger.ledgerservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionResolutionServiceImpl implements PermissionResolutionService {
    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionApiRepository permissionApiRepository;
    private final AppSettingProperty appSettingProperty;

    @Override
    @Transactional(readOnly = true)
    public Set<String> getGlobalPermissions(String userId) {
        getUserOrThrow(userId);
        return permissionRepository.findActiveGlobalPermissionCodes(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getDepartmentPermissions(String userId, String departmentId) {
        getUserOrThrow(userId);
        if (!StringUtils.hasText(departmentId)) {
            return Set.of();
        }
        return permissionRepository.findActiveDepartmentPermissionCodes(userId, departmentId.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(String userId, String permissionCode, String departmentId) {
        User user = getUserOrThrow(userId);
        if (!UserStatus.ACTIVE.equals(user.getStatus())) {
            return false;
        }
        if (permissionRepository.existsActiveGlobalPermission(userId, permissionCode)) {
            return true;
        }
        if (!StringUtils.hasText(departmentId)) {
            return false;
        }
        return permissionRepository.existsActiveDepartmentPermission(userId, departmentId.trim(), permissionCode);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasApiPermission(String userId, String method, String uri) {
        User user = getUserOrThrow(userId);
        if (!UserStatus.ACTIVE.equals(user.getStatus())) {
            return false;
        }
        String normalizedMethod = normalizeMethod(method);
        String normalizedUri = normalizeUri(uri);
        List<PermissionApi> matchedApis = findMatchingActivePermissionApis(normalizedMethod, normalizedUri);
        if (matchedApis.isEmpty()) {
            return false;
        }
        if (matchedApis.stream().anyMatch(api -> Boolean.TRUE.equals(api.getIsAllow()))) {
            return true;
        }
        Set<String> permissionIds = matchedApis.stream().map(PermissionApi::getPermissionId).filter(StringUtils::hasText).collect(Collectors.toSet());
        if (permissionIds.isEmpty()) {
            return false;
        }
        if (permissionRepository.existsAnyActiveGlobalPermission(userId, permissionIds)) {
            return true;
        }
        return permissionRepository.existsAnyActiveDepartmentPermission(userId, permissionIds);
    }

    private List<PermissionApi> findMatchingActivePermissionApis(String method, String uri) {
        String currentServiceCode = normalizeServiceCode(appSettingProperty.getArtifact());
        return permissionApiRepository.findAllByHttpMethodIgnoreCaseAndStatusOrderByPriorityDescIdAsc(method, RecordStatus.ACTIVE).stream()
                .filter(api -> currentServiceCode.equals(normalizeServiceCode(api.getServiceCode())))
                .filter(api -> matches(api, uri))
                .toList();
    }

    private boolean matches(PermissionApi api, String uri) {
        if (api.getMatchType() == PermissionMatchType.EXACT) {
            return normalizeUri(api.getUriPattern()).equals(uri);
        }
        if (api.getMatchType() == PermissionMatchType.ANT_PATH) {
            return ANT_PATH_MATCHER.match(normalizeUri(api.getUriPattern()), uri);
        }
        return false;
    }

    private User getUserOrThrow(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private String normalizeMethod(String method) {
        if (!StringUtils.hasText(method)) {
            return "";
        }
        return method.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeUri(String uri) {
        if (!StringUtils.hasText(uri)) {
            return "/";
        }
        String normalized = uri.trim();
        int queryIdx = normalized.indexOf('?');
        if (queryIdx >= 0) {
            normalized = normalized.substring(0, queryIdx);
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        normalized = normalized.replaceAll("/{2,}", "/");
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String normalizeServiceCode(String value) {
        if (!StringUtils.hasText(value)) {
            return "LEDGER_SERVICE";
        }
        return value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }
}
