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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionResolutionServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private PermissionApiRepository permissionApiRepository;

    private PermissionResolutionServiceImpl service;

    @BeforeEach
    void setUp() {
        AppSettingProperty appSettingProperty = new AppSettingProperty();
        appSettingProperty.setArtifact("ledger-service");
        service = new PermissionResolutionServiceImpl(userRepository, permissionRepository, permissionApiRepository, appSettingProperty);
    }

    @Test
    void hasPermissionByCodeUserNotFoundThrows() {
        when(userRepository.findById("user-1")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.hasPermission("user-1", "USER_VIEW", null));

        org.junit.jupiter.api.Assertions.assertEquals(MessageCode.USER_NOT_FOUND.getCode(), exception.getCode());
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void hasPermissionByCodeInactiveUserReturnsFalse() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(User.builder().id("user-1").status(UserStatus.INACTIVE).build()));

        assertFalse(service.hasPermission("user-1", "USER_VIEW", null));
    }

    @Test
    void hasApiPermissionWithoutMappingReturnsFalse() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(User.builder().id("user-1").status(UserStatus.ACTIVE).build()));
        when(permissionApiRepository.findAllByHttpMethodIgnoreCaseAndStatusOrderByPriorityDescIdAsc("GET", RecordStatus.ACTIVE)).thenReturn(List.of());

        assertFalse(service.hasApiPermission("user-1", "get", "/api/v1/users?x=1"));
    }

    @Test
    void hasApiPermissionWithGlobalRoleReturnsTrue() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(User.builder().id("user-1").status(UserStatus.ACTIVE).build()));
        when(permissionApiRepository.findAllByHttpMethodIgnoreCaseAndStatusOrderByPriorityDescIdAsc("GET", RecordStatus.ACTIVE))
                .thenReturn(List.of(api("permission-1", "GET", "/api/v1/users", PermissionMatchType.EXACT, RecordStatus.ACTIVE)));
        when(permissionRepository.existsAnyActiveGlobalPermission("user-1", Set.of("permission-1"))).thenReturn(true);

        assertTrue(service.hasApiPermission("user-1", "get", "/api/v1/users?foo=bar"));
    }

    @Test
    void hasApiPermissionWithDepartmentRoleReturnsTrue() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(User.builder().id("user-1").status(UserStatus.ACTIVE).build()));
        when(permissionApiRepository.findAllByHttpMethodIgnoreCaseAndStatusOrderByPriorityDescIdAsc("POST", RecordStatus.ACTIVE))
                .thenReturn(List.of(api("permission-2", "POST", "/api/v1/departments/**", PermissionMatchType.ANT_PATH, RecordStatus.ACTIVE)));
        when(permissionRepository.existsAnyActiveGlobalPermission("user-1", Set.of("permission-2"))).thenReturn(false);
        when(permissionRepository.existsAnyActiveDepartmentPermission("user-1", Set.of("permission-2"))).thenReturn(true);

        assertTrue(service.hasApiPermission("user-1", "post", "/api/v1/departments/dep-1/users"));
    }

    @Test
    void hasApiPermissionDepartmentInactiveReturnsFalse() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(User.builder().id("user-1").status(UserStatus.ACTIVE).build()));
        when(permissionApiRepository.findAllByHttpMethodIgnoreCaseAndStatusOrderByPriorityDescIdAsc("GET", RecordStatus.ACTIVE))
                .thenReturn(List.of(api("permission-3", "GET", "/api/v1/reports/**", PermissionMatchType.ANT_PATH, RecordStatus.ACTIVE)));
        when(permissionRepository.existsAnyActiveGlobalPermission("user-1", Set.of("permission-3"))).thenReturn(false);
        when(permissionRepository.existsAnyActiveDepartmentPermission("user-1", Set.of("permission-3"))).thenReturn(false);

        assertFalse(service.hasApiPermission("user-1", "GET", "/api/v1/reports/monthly"));
    }

    @Test
    void hasApiPermissionMultipleMatchesOnlyOnePermissionNeeded() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(User.builder().id("user-1").status(UserStatus.ACTIVE).build()));
        when(permissionApiRepository.findAllByHttpMethodIgnoreCaseAndStatusOrderByPriorityDescIdAsc("GET", RecordStatus.ACTIVE))
                .thenReturn(List.of(
                        api("permission-1", "GET", "/api/v1/users/**", PermissionMatchType.ANT_PATH, RecordStatus.ACTIVE),
                        api("permission-2", "GET", "/api/v1/users/123", PermissionMatchType.EXACT, RecordStatus.ACTIVE)
                ));
        when(permissionRepository.existsAnyActiveGlobalPermission("user-1", Set.of("permission-1", "permission-2"))).thenReturn(true);

        assertTrue(service.hasApiPermission("user-1", "GET", "/api/v1/users/123"));
    }

    private PermissionApi api(String permissionId, String method, String uri, PermissionMatchType matchType, RecordStatus status) {
        return PermissionApi.builder()
                .permissionId(permissionId)
                .httpMethod(method)
                .uriPattern(uri)
                .matchType(matchType)
                .serviceCode("LEDGER_SERVICE")
                .status(status)
                .priority(10)
                .isAllow(false)
                .build();
    }
}
