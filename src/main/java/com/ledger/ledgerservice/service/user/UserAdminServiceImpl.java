package com.ledger.ledgerservice.service.user;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.model.dto.request.AdminUserSearchRequest;
import com.ledger.ledgerservice.model.dto.response.AdminUserDetailResponse;
import com.ledger.ledgerservice.model.dto.response.AdminUserItemResponse;
import com.ledger.ledgerservice.model.entity.Group;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.entity.UserGroup;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.model.enums.UserStatus;
import com.ledger.ledgerservice.repository.GroupRepository;
import com.ledger.ledgerservice.repository.UserGroupRepository;
import com.ledger.ledgerservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final GroupRepository groupRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserItemResponse> getUsers(AdminUserSearchRequest request) {
        int requestPage = request == null || request.getPage() == null ? 1 : request.getPage();
        int safePage = requestPage <= 0 ? DEFAULT_PAGE : requestPage - 1;
        int safeSize = request == null || request.getSize() == null || request.getSize() <= 0
                ? DEFAULT_SIZE : Math.min(request.getSize(), MAX_SIZE);
        String normalizedKeyword = normalizeKeyword(request == null ? null : request.getKeyword());
        UserStatus status = request == null ? null : request.getStatus();

        Page<User> result = userRepository.searchUsers(
                normalizedKeyword,
                status,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        List<User> users = result.getContent();
        Map<String, UserGroup> userGroupByUserId = mapUserGroups(users);
        Map<String, Group> groupById = mapGroups(userGroupByUserId.values().stream()
                .map(UserGroup::getGroupId)
                .collect(Collectors.toSet()));

        List<AdminUserItemResponse> items = users.stream()
                .map(user -> toListItem(user, userGroupByUserId.get(user.getId()), groupById))
                .toList();

        return new PageImpl<>(items, result.getPageable(), result.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUserDetail(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        UserGroup userGroup = userGroupRepository.findActiveByUserIdAt(userId, RecordStatus.ACTIVE, LocalDateTime.now())
                .stream()
                .findFirst()
                .orElse(null);
        Group group = userGroup == null ? null : groupRepository.findById(userGroup.getGroupId()).orElse(null);

        return AdminUserDetailResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .userType(user.getUserType())
                .requireChange(user.getRequireChange())
                .status(user.getStatus())
                .groupId(group != null ? group.getId() : null)
                .groupCode(group != null ? group.getCode() : null)
                .groupName(group != null ? group.getName() : null)
                .createdAt(user.getCreatedAt())
                .createdBy(user.getCreatedBy())
                .updatedAt(user.getUpdatedAt())
                .updatedBy(user.getUpdatedBy())
                .build();
    }

    @Override
    @Transactional
    public void lockUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        validateNotSelf(user);

        if (user.getStatus() == UserStatus.LOCKED) {
            return;
        }

        user.setStatus(UserStatus.LOCKED);
        touchAudit(user);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unlockUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (user.getStatus() == UserStatus.ACTIVE) {
            return;
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setLockedUntil(null);
        touchAudit(user);
        userRepository.save(user);
    }

    private Map<String, UserGroup> mapUserGroups(List<User> users) {
        Set<String> userIds = users.stream().map(User::getId).collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Map.of();
        }
        Map<String, UserGroup> result = new LinkedHashMap<>();
        for (UserGroup userGroup : userGroupRepository.findActiveByUserIdInAt(userIds, RecordStatus.ACTIVE, LocalDateTime.now())) {
            result.putIfAbsent(userGroup.getUserId(), userGroup);
        }
        return result;
    }

    private Map<String, Group> mapGroups(Set<String> groupIds) {
        if (groupIds.isEmpty()) {
            return Map.of();
        }
        return groupRepository.findAllById(groupIds).stream()
                .collect(Collectors.toMap(Group::getId, Function.identity()));
    }

    private AdminUserItemResponse toListItem(User user, UserGroup userGroup, Map<String, Group> groupById) {
        Group group = userGroup == null ? null : groupById.get(userGroup.getGroupId());
        return AdminUserItemResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .userType(user.getUserType())
                .requireChange(user.getRequireChange())
                .status(user.getStatus())
                .groupId(group != null ? group.getId() : null)
                .groupCode(group != null ? group.getCode() : null)
                .groupName(group != null ? group.getName() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private void touchAudit(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy(resolveActor());
    }

    private String resolveActor() {
        String username = RequestContextHolder.get() != null ? RequestContextHolder.get().getUsername() : null;
        return StringUtils.hasText(username) ? username : "system";
    }

    private void validateNotSelf(User targetUser) {
        String actor = resolveActor();
        if (StringUtils.hasText(actor) && actor.equalsIgnoreCase(targetUser.getUsername())) {
            throw new BusinessException(MessageCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        }
    }

    private String normalizeKeyword(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
