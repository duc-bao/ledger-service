package com.ledger.ledgerservice.service.user;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.context.RequestContext;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.model.dto.request.ChangePasswordRequest;
import com.ledger.ledgerservice.model.dto.request.UpdateMyProfileRequest;
import com.ledger.ledgerservice.model.dto.response.UserProfileResponse;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.security.JwtUserPrincipal;
import com.ledger.ledgerservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        user.setUpdatedBy(user.getUsername());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
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
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .userType(user.getUserType())
                .requireChange(user.getRequireChange())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
