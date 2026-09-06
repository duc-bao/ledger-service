package com.ledger.ledgerservice.service.user;

import com.ledger.ledgerservice.model.context.RequestContext;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.model.dto.request.UpdateTwoFactorRequest;
import com.ledger.ledgerservice.model.dto.response.UserProfileResponse;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.enums.UserStatus;
import com.ledger.ledgerservice.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserProfileServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserProfileServiceImpl(userRepository, passwordEncoder);
        RequestContextHolder.set(RequestContext.builder().username("user1").build());
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Test
    void updateTwoFactorPersistsPreferenceOnCurrentUser() {
        User user = User.builder().id("user-1").username("user1").password("encodedPass").status(UserStatus.ACTIVE).twoFactorEnabled(true).build();
        UpdateTwoFactorRequest request = new UpdateTwoFactorRequest();
        request.setEnabled(false);
        request.setPassword("correctPassword");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "encodedPass")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse response = service.updateTwoFactor(request);

        assertFalse(response.getTwoFactorEnabled());
    }

    @Test
    void updateTwoFactorThrowsExceptionWhenDisablingWithoutPassword() {
        User user = User.builder().id("user-1").username("user1").password("encodedPass").status(UserStatus.ACTIVE).twoFactorEnabled(true).build();
        UpdateTwoFactorRequest request = new UpdateTwoFactorRequest();
        request.setEnabled(false);
        request.setPassword("wrongPassword");
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPass")).thenReturn(false);

        org.junit.jupiter.api.Assertions.assertThrows(com.ledger.ledgerservice.exception.BusinessException.class,
                () -> service.updateTwoFactor(request));
    }
}
