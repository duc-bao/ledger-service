package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.request.AdminCreateUserRequest;
import com.ledger.ledgerservice.model.dto.request.AssignUserRoleRequest;
import com.ledger.ledgerservice.model.dto.response.AdminCreateUserResponse;
import com.ledger.ledgerservice.model.dto.response.UserRoleResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AccessControlAdminService {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserRoleResponse assignRoleForUser(AssignUserRoleRequest request) {
        String userId = trimRequired(request.getUserId(), MessageCode.USER_ID_REQUIRED);
        String groupId = trimRequired(request.getGroupId(), MessageCode.GROUP_ID_REQUIRED);
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(MessageCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        Group group = groupRepository.findByIdAndStatus(groupId, RecordStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(MessageCode.GROUP_NOT_FOUND, HttpStatus.NOT_FOUND));
        UserGroup mapping = userGroupRepository.findByUserIdAndGroupId(userId, groupId)
                .orElse(UserGroup.builder().userId(userId).groupId(group.getId()).build());
        mapping.setStatus(RecordStatus.ACTIVE);
        userGroupRepository.save(mapping);
        return UserRoleResponse.builder().userId(userId).groupId(group.getId()).build();
    }

    @Transactional
    public AdminCreateUserResponse createUserByAdmin(AdminCreateUserRequest request) {
        String username = trimRequired(request.getUsername(), MessageCode.BAD_REQUEST);
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new BusinessException(MessageCode.USERNAME_EXISTS, HttpStatus.CONFLICT);
        }
        if (StringUtils.hasText(request.getEmail()) && userRepository.existsByEmailIgnoreCase(request.getEmail().trim())) {
            throw new BusinessException(MessageCode.USER_EMAIL_EXISTS, HttpStatus.CONFLICT);
        }
        if (StringUtils.hasText(request.getPhone()) && userRepository.existsByPhone(request.getPhone().trim())) {
            throw new BusinessException(MessageCode.USER_PHONE_EXISTS, HttpStatus.CONFLICT);
        }

        Group group = null;
        if (StringUtils.hasText(request.getGroupId())) {
            group = groupRepository.findByIdAndStatus(request.getGroupId().trim(), RecordStatus.ACTIVE)
                    .orElseThrow(() -> new BusinessException(MessageCode.GROUP_NOT_FOUND, HttpStatus.NOT_FOUND));
        }

        User user = userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(request.getPassword()))
                .email(trimNullable(request.getEmail()))
                .phone(trimNullable(request.getPhone()))
                .fullName(trimNullable(request.getFullName()))
                .userType(trimNullable(request.getUserType()))
                .requireChange(Boolean.TRUE.equals(request.getRequireChange()))
                .status(UserStatus.ACTIVE)
                .build());

        if (group != null) {
            userGroupRepository.save(UserGroup.builder().userId(user.getId()).groupId(group.getId()).status(RecordStatus.ACTIVE).build());
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

    private String trimRequired(String value, MessageCode code) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(code, HttpStatus.BAD_REQUEST);
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