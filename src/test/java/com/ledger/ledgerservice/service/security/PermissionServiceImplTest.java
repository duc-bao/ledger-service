package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.request.PermissionCreateRequest;
import com.ledger.ledgerservice.model.dto.request.PermissionSearchRequest;
import com.ledger.ledgerservice.model.dto.response.PageResponse;
import com.ledger.ledgerservice.model.dto.response.PermissionResponse;
import com.ledger.ledgerservice.model.entity.Permission;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.model.mapper.PermissionMapper;
import com.ledger.ledgerservice.repository.MenuPermissionRepository;
import com.ledger.ledgerservice.repository.PermissionApiRepository;
import com.ledger.ledgerservice.repository.PermissionRepository;
import com.ledger.ledgerservice.repository.RolePermissionRepository;
import com.ledger.ledgerservice.model.entity.MenuPermission;
import com.ledger.ledgerservice.repository.MenuRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private RolePermissionRepository rolePermissionRepository;
    @Mock
    private PermissionApiRepository permissionApiRepository;
    @Mock
    private MenuPermissionRepository menuPermissionRepository;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    @Test
    void createSuccessNormalizesCode() {
        PermissionCreateRequest request = PermissionCreateRequest.builder()
                .code(" user_create ")
                .name("Create user")
                .moduleCode("user_management")
                .actionCode("create")
                .resourceType("user")
                .description(" desc ")
                .build();
        Permission entity = Permission.builder().build();
        Permission saved = Permission.builder()
                .id("permission-1")
                .code("USER_CREATE")
                .name("Create user")
                .moduleCode("USER_MANAGEMENT")
                .actionCode("CREATE")
                .resourceType("USER")
                .status(RecordStatus.ACTIVE)
                .build();
        PermissionResponse response = PermissionResponse.builder().id("permission-1").code("USER_CREATE").build();

        when(permissionRepository.existsByCodeIgnoreCase("USER_CREATE")).thenReturn(false);
        when(permissionMapper.toEntity(request)).thenReturn(entity);
        when(permissionRepository.save(any(Permission.class))).thenReturn(saved);
        when(permissionMapper.toResponse(saved)).thenReturn(response);

        PermissionResponse actual = permissionService.create(request);

        assertEquals("permission-1", actual.getId());
        ArgumentCaptor<Permission> captor = ArgumentCaptor.forClass(Permission.class);
        verify(permissionRepository).save(captor.capture());
        assertEquals("USER_CREATE", captor.getValue().getCode());
        assertEquals("USER_MANAGEMENT", captor.getValue().getModuleCode());
        assertEquals("CREATE", captor.getValue().getActionCode());
        assertEquals(RecordStatus.ACTIVE, captor.getValue().getStatus());
    }

    @Test
    void createSuccessWithoutModuleAndActionCodeAndAutoAssignsMenu() {
        PermissionCreateRequest request = PermissionCreateRequest.builder()
                .code("REPORT_EXPORT")
                .name("Export report")
                .menuId("menu-123")
                .displayAction("Xuất file")
                .build();
        Permission entity = Permission.builder().build();
        Permission saved = Permission.builder()
                .id("perm-export")
                .code("REPORT_EXPORT")
                .name("Export report")
                .status(RecordStatus.ACTIVE)
                .build();
        PermissionResponse response = PermissionResponse.builder().id("perm-export").code("REPORT_EXPORT").build();

        when(permissionRepository.existsByCodeIgnoreCase("REPORT_EXPORT")).thenReturn(false);
        when(menuRepository.existsById("menu-123")).thenReturn(true);
        when(permissionMapper.toEntity(request)).thenReturn(entity);
        when(permissionRepository.save(any(Permission.class))).thenReturn(saved);
        when(menuPermissionRepository.existsByMenuIdAndPermissionId("menu-123", "perm-export")).thenReturn(false);
        when(permissionMapper.toResponse(saved)).thenReturn(response);

        PermissionResponse actual = permissionService.create(request);

        assertEquals("perm-export", actual.getId());
        ArgumentCaptor<Permission> permCaptor = ArgumentCaptor.forClass(Permission.class);
        verify(permissionRepository).save(permCaptor.capture());
        assertNull(permCaptor.getValue().getModuleCode());
        assertNull(permCaptor.getValue().getActionCode());

        ArgumentCaptor<MenuPermission> menuPermCaptor = ArgumentCaptor.forClass(MenuPermission.class);
        verify(menuPermissionRepository).save(menuPermCaptor.capture());
        assertEquals("menu-123", menuPermCaptor.getValue().getMenuId());
        assertEquals("perm-export", menuPermCaptor.getValue().getPermissionId());
        assertEquals("Xuất file", menuPermCaptor.getValue().getDisplayAction());
    }

    @Test
    void createThrowsWhenMenuNotFound() {
        PermissionCreateRequest request = PermissionCreateRequest.builder()
                .code("REPORT_EXPORT")
                .name("Export report")
                .menuId("missing-menu")
                .build();

        when(permissionRepository.existsByCodeIgnoreCase("REPORT_EXPORT")).thenReturn(false);
        when(menuRepository.existsById("missing-menu")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> permissionService.create(request));
        assertEquals(MessageCode.MENU_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void createDuplicateCodeThrowsConflict() {
        PermissionCreateRequest request = PermissionCreateRequest.builder()
                .code("USER_CREATE")
                .name("Create user")
                .moduleCode("USER")
                .actionCode("CREATE")
                .build();
        when(permissionRepository.existsByCodeIgnoreCase("USER_CREATE")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> permissionService.create(request));

        assertEquals(MessageCode.PERMISSION_CODE_EXISTS.getCode(), exception.getCode());
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void getByIdNotFoundThrows() {
        when(permissionRepository.findById("missing")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> permissionService.getById("missing"));

        assertEquals(MessageCode.PERMISSION_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void searchReturnsPagedResponse() {
        Permission permission = Permission.builder().id("permission-1").code("USER_VIEW").build();
        PermissionResponse response = PermissionResponse.builder().id("permission-1").code("USER_VIEW").build();
        when(permissionRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(permission), PageRequest.of(0, 10), 1));
        when(permissionMapper.toResponses(List.of(permission))).thenReturn(List.of(response));

        PageResponse<PermissionResponse> actual = permissionService.search(new PermissionSearchRequest(), null);

        assertEquals(1L, actual.getTotalElements());
        assertEquals(1, actual.getItems().size());
        assertEquals("USER_VIEW", actual.getItems().get(0).getCode());
    }

    @Test
    void deleteWhenPermissionInUseThrowsConflict() {
        Permission permission = Permission.builder().id("permission-1").code("USER_VIEW").build();
        when(permissionRepository.findById("permission-1")).thenReturn(Optional.of(permission));
        when(rolePermissionRepository.existsByPermissionId("permission-1")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> permissionService.delete("permission-1"));

        assertEquals(MessageCode.PERMISSION_IN_USE.getCode(), exception.getCode());
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
}
