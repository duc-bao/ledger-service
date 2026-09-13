package com.ledger.ledgerservice.service.user;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.context.RequestContext;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.model.dto.request.ChangePasswordRequest;
import com.ledger.ledgerservice.model.dto.request.UpdateTwoFactorRequest;
import com.ledger.ledgerservice.model.dto.request.UpdateMyProfileRequest;
import com.ledger.ledgerservice.model.dto.response.UserProfileResponse;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.security.JwtUserPrincipal;
import com.ledger.ledgerservice.repository.UserRepository;
import com.ledger.ledgerservice.config.properties.JwtProperties;
import com.ledger.ledgerservice.service.auth.TokenBlacklistService;
import com.ledger.ledgerservice.util.JwtUtils;
import com.ledger.ledgerservice.model.entity.DepartmentEntity;
import com.ledger.ledgerservice.model.entity.DepartmentUserEntity;
import com.ledger.ledgerservice.model.entity.Group;
import com.ledger.ledgerservice.model.entity.UserGroup;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.repository.DepartmentRepository;
import com.ledger.ledgerservice.repository.DepartmentUserRepository;
import com.ledger.ledgerservice.repository.GroupRepository;
import com.ledger.ledgerservice.repository.UserGroupRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserProfileServiceImpl implements UserProfileService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtProperties jwtProperties;
    private final HttpServletRequest httpServletRequest;
    private final UserGroupRepository userGroupRepository;
    private final GroupRepository groupRepository;
    private final DepartmentUserRepository departmentUserRepository;
    private final DepartmentRepository departmentRepository;

    public UserProfileServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this(userRepository, passwordEncoder, null, null, null, null, null, null, null);
    }

    @Autowired
    public UserProfileServiceImpl(UserRepository userRepository,
                                  PasswordEncoder passwordEncoder,
                                  @Autowired(required = false) TokenBlacklistService tokenBlacklistService,
                                  @Autowired(required = false) JwtProperties jwtProperties,
                                  @Autowired(required = false) HttpServletRequest httpServletRequest,
                                  @Autowired(required = false) UserGroupRepository userGroupRepository,
                                  @Autowired(required = false) GroupRepository groupRepository,
                                  @Autowired(required = false) DepartmentUserRepository departmentUserRepository,
                                  @Autowired(required = false) DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtProperties = jwtProperties;
        this.httpServletRequest = httpServletRequest;
        this.userGroupRepository = userGroupRepository;
        this.groupRepository = groupRepository;
        this.departmentUserRepository = departmentUserRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {
        User user = getCurrentUser();
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(UpdateMyProfileRequest request) {
        User user = getCurrentUser();

        String normalizedEmail = normalize(request.getEmail());
        if (StringUtils.hasText(normalizedEmail)) {
            userRepository.findByEmail(normalizedEmail)
                    .filter(exist -> !exist.getId().equals(user.getId()))
                    .ifPresent(exist -> {
                        throw new BusinessException(MessageCode.EXISTED, HttpStatus.CONFLICT);
                    });
        }

        String normalizedPhone = normalize(request.getPhone());
        if (StringUtils.hasText(normalizedPhone)) {
            userRepository.findByPhone(normalizedPhone)
                    .filter(exist -> !exist.getId().equals(user.getId()))
                    .ifPresent(exist -> {
                        throw new BusinessException(MessageCode.EXISTED, HttpStatus.CONFLICT);
                    });
        }

        user.setEmail(normalizedEmail);
        user.setPhone(normalizedPhone);
        user.setFullName(normalize(request.getFullName()));
        user.setUpdatedBy(user.getUsername());
        user.setUpdatedAt(LocalDateTime.now());

        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changeMyPassword(ChangePasswordRequest request) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(MessageCode.PASSWORD_INVALID, HttpStatus.BAD_REQUEST);
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException(MessageCode.NEW_PASSWORD_DIFFERENT_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(MessageCode.CONFIRM_PASSWORD_NOT_MATCH, HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setRequireChange(false);
        user.setLastPasswordChangedAt(LocalDateTime.now());
        user.setUpdatedBy(user.getUsername());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        revokeCurrentToken();
    }

    @Override
    @Transactional
    public UserProfileResponse updateTwoFactor(UpdateTwoFactorRequest request) {
        User user = getCurrentUser();
        boolean newEnabled = Boolean.TRUE.equals(request.getEnabled());
        if (!newEnabled && Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            if (!StringUtils.hasText(request.getPassword()) || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new BusinessException(MessageCode.PASSWORD_INVALID, HttpStatus.BAD_REQUEST);
            }
        }
        user.setTwoFactorEnabled(newEnabled);
        user.setUpdatedBy(user.getUsername());
        user.setUpdatedAt(LocalDateTime.now());
        return toResponse(userRepository.save(user));
    }

    private void revokeCurrentToken() {
        if (httpServletRequest == null || tokenBlacklistService == null || jwtProperties == null) {
            return;
        }
        String authHeader = httpServletRequest.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(JwtUtils.TOKEN_PREFIX)) {
            return;
        }
        String token = authHeader.substring(JwtUtils.TOKEN_PREFIX.length()).trim();
        String secret = StringUtils.hasText(jwtProperties.getSecret()) ? jwtProperties.getSecret() : jwtProperties.getKey();
        if (StringUtils.hasText(secret) && JwtUtils.verified(token, secret)) {
            Duration remainingTtl = JwtUtils.getRemainingTtl(token, secret);
            String jti = JwtUtils.getJti(token, secret);
            if (StringUtils.hasText(jti)) {
                tokenBlacklistService.blacklistJti(jti, remainingTtl);
            } else {
                tokenBlacklistService.blacklist(token, remainingTtl);
            }
        }
    }

    private User getCurrentUser() {
        String username = getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND, HttpStatus.UNAUTHORIZED));
    }

    private String getCurrentUsername() {
        RequestContext requestContext = RequestContextHolder.get();
        return requestContext.getUsername();
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private UserProfileResponse toResponse(User user) {
        String roleId = null;
        String roleCode = null;
        String roleName = null;
        if (userGroupRepository != null && groupRepository != null) {
            List<UserGroup> userGroups = userGroupRepository.findByUserIdAndStatus(user.getId(), RecordStatus.ACTIVE);
            if (!userGroups.isEmpty()) {
                roleId = userGroups.get(0).getGroupId();
                var groupOpt = groupRepository.findById(roleId);
                if (groupOpt.isPresent()) {
                    roleCode = groupOpt.get().getCode();
                    roleName = groupOpt.get().getName();
                }
            }
        }

        String departmentId = null;
        String departmentName = null;
        if (departmentUserRepository != null && departmentRepository != null) {
            List<DepartmentUserEntity> deptUsers = departmentUserRepository.findByUserIdAndStatus(user.getId(), RecordStatus.ACTIVE);
            if (!deptUsers.isEmpty()) {
                departmentId = deptUsers.get(0).getDepartmentId();
                var deptOpt = departmentRepository.findById(departmentId);
                if (deptOpt.isPresent()) {
                    departmentName = deptOpt.get().getName();
                }
            }
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .userType(user.getUserType())
                .requireChange(user.getRequireChange())
                .twoFactorEnabled(user.getTwoFactorEnabled())
                .status(user.getStatus())
                .roleId(roleId)
                .roleCode(roleCode)
                .roleName(roleName)
                .departmentId(departmentId)
                .departmentName(departmentName)
                .lastLoginAt(user.getLastLoginAt())
                .lastPasswordChangedAt(user.getLastPasswordChangedAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
