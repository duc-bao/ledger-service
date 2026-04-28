package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.model.constant.RedisCacheConstant;
import com.ledger.ledgerservice.model.entity.Menu;
import com.ledger.ledgerservice.model.entity.Permission;
import com.ledger.ledgerservice.repository.MenuRepository;
import com.ledger.ledgerservice.repository.PermissionRepository;
import com.ledger.ledgerservice.repository.UserGroupRepository;
import com.ledger.ledgerservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private final RedissonClient redissonClient;
    private final UserGroupRepository userGroupRepository;
    private final PermissionRepository permissionRepository;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;

    public Set<String> getAuthorities(String userId, String username) {
        String resolvedUserId = resolveUserId(userId, username);
        if (!StringUtils.hasText(resolvedUserId)) {
            return Set.of();
        }

        String cacheKey = String.format(RedisCacheConstant.CACHE_KEY_FORMAT, resolvedUserId);
        try {
            RBucket<Set<String>> bucket = redissonClient.getBucket(cacheKey);
            Set<String> cached = bucket.get();
            if (cached != null) {
                return cached;
            }

            Set<String> loaded = loadAuthorities(resolvedUserId);
            bucket.set(loaded, CACHE_TTL);
            return loaded;
        } catch (Exception ex) {
            log.error("Load permissions from cache failed for userId={}", resolvedUserId, ex);
            return loadAuthorities(resolvedUserId);
        }
    }

    public void evictUserAuthoritiesCache(String userId) {
        if (!StringUtils.hasText(userId)) {
            return;
        }
        String cacheKey = String.format(RedisCacheConstant.CACHE_KEY_FORMAT, userId);
        try {
            redissonClient.getBucket(cacheKey).delete();
        } catch (Exception ex) {
            log.error("Evict permissions cache failed for userId={}", userId, ex);
        }
    }

    public void evictUsersAuthoritiesCacheAfterCommit(Collection<String> userIds) {
        Set<String> normalizedUserIds = userIds == null ? Set.of() : userIds.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (normalizedUserIds.isEmpty()) {
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    normalizedUserIds.forEach(PermissionService.this::evictUserAuthoritiesCache);
                }
            });
            return;
        }

        normalizedUserIds.forEach(this::evictUserAuthoritiesCache);
    }

    private String resolveUserId(String userId, String username) {
        if (StringUtils.hasText(userId)) {
            return userId;
        }
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return userRepository.findByUsername(username)
                .map(u -> u.getId())
                .orElse(null);
    }

    private Set<String> loadAuthorities(String userId) {
        List<String> groupIds = userGroupRepository.findByUserId(userId).stream()
                .map(ug -> ug.getGroupId())
                .filter(StringUtils::hasText)
                .toList();

        List<Permission> permissions = new ArrayList<>(permissionRepository.findByUserId(userId));
        if (!groupIds.isEmpty()) {
            permissions.addAll(permissionRepository.findByGroupIdIn(groupIds));
        }

        Set<String> menuIds = permissions.stream()
                .map(Permission::getMenuId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        Map<String, String> menuCodeById = menuRepository.findAllById(menuIds).stream()
                .filter(menu -> StringUtils.hasText(menu.getId()) && StringUtils.hasText(menu.getCode()))
                .collect(Collectors.toMap(Menu::getId, m -> m.getCode().toUpperCase()));

        Set<String> authorities = new HashSet<>();
        for (Permission permission : permissions) {
            String menuCode = menuCodeById.get(permission.getMenuId());
            if (!StringUtils.hasText(menuCode) || permission.getActions() == null) {
                continue;
            }
            for (String action : permission.getActions()) {
                if (StringUtils.hasText(action)) {
                    authorities.add(menuCode + ":" + action.toUpperCase());
                }
            }
        }
        return authorities;
    }
}
