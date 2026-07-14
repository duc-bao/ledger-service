package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.config.properties.AppSettingProperty;
import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.request.PermissionApiCreateRequest;
import com.ledger.ledgerservice.model.dto.response.PermissionApiResponse;
import com.ledger.ledgerservice.model.entity.Permission;
import com.ledger.ledgerservice.model.entity.PermissionApi;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.PermissionMatchType;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.model.mapper.PermissionApiMapper;
import com.ledger.ledgerservice.repository.PermissionApiRepository;
import com.ledger.ledgerservice.repository.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionApiServiceImplTest {
    @Mock
    private PermissionApiRepository permissionApiRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private PermissionApiMapper permissionApiMapper;

    private AppSettingProperty appSettingProperty;

    @InjectMocks
    private PermissionApiServiceImpl permissionApiService;

    @BeforeEach
    void setUp() {
        appSettingProperty = new AppSettingProperty();
        appSettingProperty.setArtifact("ledger-service");
        permissionApiService = new PermissionApiServiceImpl(permissionApiRepository, permissionRepository, permissionApiMapper, appSettingProperty);
    }

    @Test
    void createSuccessNormalizesMethodAndUri() {
        Permission permission = Permission.builder().id("permission-1").code("USER_VIEW").name("View user").status(RecordStatus.ACTIVE).build();
        PermissionApiCreateRequest request = PermissionApiCreateRequest.builder()
                .permissionId("permission-1")
                .httpMethod("get")
                .uriPattern("api/v1/users?id=1")
                .matchType(PermissionMatchType.EXACT)
                .serviceCode("ledger-service")
                .build();
        PermissionApi entity = PermissionApi.builder().build();
        PermissionApi saved = PermissionApi.builder()
                .id("api-1")
                .permissionId("permission-1")
                .httpMethod("GET")
                .uriPattern("/api/v1/users")
                .serviceCode("LEDGER_SERVICE")
                .matchType(PermissionMatchType.EXACT)
                .status(RecordStatus.ACTIVE)
                .priority(0)
                .isAllow(false)
                .build();
        PermissionApiResponse response = PermissionApiResponse.builder().id("api-1").build();

        when(permissionRepository.findById("permission-1")).thenReturn(Optional.of(permission));
        when(permissionApiRepository.existsByPermissionIdAndHttpMethodIgnoreCaseAndUriPatternAndServiceCodeIgnoreCase("permission-1", "GET", "/api/v1/users", "LEDGER_SERVICE")).thenReturn(false);
        when(permissionApiMapper.toEntity(request)).thenReturn(entity);
        when(permissionApiRepository.save(any(PermissionApi.class))).thenReturn(saved);
        when(permissionApiMapper.toResponse(saved)).thenReturn(response);
        when(permissionRepository.findById("permission-1")).thenReturn(Optional.of(permission));

        PermissionApiResponse actual = permissionApiService.create(request);

        assertEquals("api-1", actual.getId());
        ArgumentCaptor<PermissionApi> captor = ArgumentCaptor.forClass(PermissionApi.class);
        verify(permissionApiRepository).save(captor.capture());
        assertEquals("GET", captor.getValue().getHttpMethod());
        assertEquals("/api/v1/users", captor.getValue().getUriPattern());
        assertEquals("LEDGER_SERVICE", captor.getValue().getServiceCode());
    }

    @Test
    void createDuplicateThrowsConflict() {
        Permission permission = Permission.builder().id("permission-1").status(RecordStatus.ACTIVE).build();
        PermissionApiCreateRequest request = PermissionApiCreateRequest.builder()
                .permissionId("permission-1")
                .httpMethod("GET")
                .uriPattern("/api/v1/users")
                .matchType(PermissionMatchType.EXACT)
                .serviceCode("LEDGER_SERVICE")
                .build();

        when(permissionRepository.findById("permission-1")).thenReturn(Optional.of(permission));
        when(permissionApiRepository.existsByPermissionIdAndHttpMethodIgnoreCaseAndUriPatternAndServiceCodeIgnoreCase("permission-1", "GET", "/api/v1/users", "LEDGER_SERVICE")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> permissionApiService.create(request));

        assertEquals(MessageCode.PERMISSION_API_EXISTS.getCode(), exception.getCode());
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
}
