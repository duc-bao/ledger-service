package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.request.RolePermissionCreateRequest;
import com.ledger.ledgerservice.model.dto.response.RolePermissionResponse;
import com.ledger.ledgerservice.model.entity.Group;
import com.ledger.ledgerservice.model.entity.Permission;
import com.ledger.ledgerservice.model.entity.RolePermission;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.model.mapper.PermissionMapper;
import com.ledger.ledgerservice.model.mapper.RolePermissionMapper;
import com.ledger.ledgerservice.repository.GroupRepository;
import com.ledger.ledgerservice.repository.PermissionRepository;
import com.ledger.ledgerservice.repository.RolePermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolePermissionServiceImplTest {
    @Mock
    private RolePermissionRepository rolePermissionRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private RolePermissionMapper rolePermissionMapper;
    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private RolePermissionServiceImpl rolePermissionService;

    @Test
    void assignSuccess() {
        Group group = Group.builder().id("role-1").code("ADMIN").name("Admin").status(RecordStatus.ACTIVE).build();
        Permission permission = Permission.builder().id("permission-1").code("USER_CREATE").name("Create user").moduleCode("USER").actionCode("CREATE").status(RecordStatus.ACTIVE).build();
        RolePermission entity = RolePermission.builder().id("rp-1").groupId("role-1").permissionId("permission-1").status(RecordStatus.ACTIVE).build();
        RolePermissionResponse response = RolePermissionResponse.builder().id("rp-1").build();
        RolePermissionCreateRequest request = RolePermissionCreateRequest.builder().groupId("role-1").permissionId("permission-1").build();

        when(groupRepository.findById("role-1")).thenReturn(Optional.of(group));
        when(permissionRepository.findById("permission-1")).thenReturn(Optional.of(permission));
        when(rolePermissionRepository.existsByGroupIdAndPermissionId("role-1", "permission-1")).thenReturn(false);
        when(rolePermissionRepository.save(any(RolePermission.class))).thenReturn(entity);
        when(rolePermissionMapper.toResponse(entity)).thenReturn(response);

        RolePermissionResponse actual = rolePermissionService.assign(request);

        assertEquals("rp-1", actual.getId());
        assertEquals("ADMIN", actual.getGroupCode());
        assertEquals("USER_CREATE", actual.getPermissionCode());
    }

    @Test
    void assignRoleNotFoundThrows() {
        when(groupRepository.findById("role-1")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> rolePermissionService.assign(RolePermissionCreateRequest.builder().groupId("role-1").permissionId("permission-1").build()));

        assertEquals(MessageCode.GROUP_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void assignPermissionNotFoundThrows() {
        Group group = Group.builder().id("role-1").status(RecordStatus.ACTIVE).build();
        when(groupRepository.findById("role-1")).thenReturn(Optional.of(group));
        when(permissionRepository.findById("permission-1")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> rolePermissionService.assign(RolePermissionCreateRequest.builder().groupId("role-1").permissionId("permission-1").build()));

        assertEquals(MessageCode.PERMISSION_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void assignDuplicateThrows() {
        Group group = Group.builder().id("role-1").status(RecordStatus.ACTIVE).build();
        Permission permission = Permission.builder().id("permission-1").status(RecordStatus.ACTIVE).build();
        when(groupRepository.findById("role-1")).thenReturn(Optional.of(group));
        when(permissionRepository.findById("permission-1")).thenReturn(Optional.of(permission));
        when(rolePermissionRepository.existsByGroupIdAndPermissionId("role-1", "permission-1")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> rolePermissionService.assign(RolePermissionCreateRequest.builder().groupId("role-1").permissionId("permission-1").build()));

        assertEquals(MessageCode.ROLE_PERMISSION_EXISTS.getCode(), exception.getCode());
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void assignInactiveRoleThrows() {
        Group group = Group.builder().id("role-1").status(RecordStatus.INACTIVE).build();
        when(groupRepository.findById("role-1")).thenReturn(Optional.of(group));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> rolePermissionService.assign(RolePermissionCreateRequest.builder().groupId("role-1").permissionId("permission-1").build()));

        assertEquals(MessageCode.GROUP_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void assignInactivePermissionThrows() {
        Group group = Group.builder().id("role-1").status(RecordStatus.ACTIVE).build();
        Permission permission = Permission.builder().id("permission-1").status(RecordStatus.INACTIVE).build();
        when(groupRepository.findById("role-1")).thenReturn(Optional.of(group));
        when(permissionRepository.findById("permission-1")).thenReturn(Optional.of(permission));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> rolePermissionService.assign(RolePermissionCreateRequest.builder().groupId("role-1").permissionId("permission-1").build()));

        assertEquals(MessageCode.PERMISSION_STATUS_INVALID.getCode(), exception.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
