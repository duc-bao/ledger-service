package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.request.DepartmentUserRoleCreateRequest;
import com.ledger.ledgerservice.model.dto.response.DepartmentUserRoleResponse;
import com.ledger.ledgerservice.model.dto.response.PermissionResponse;
import com.ledger.ledgerservice.model.entity.DepartmentEntity;
import com.ledger.ledgerservice.model.entity.DepartmentUserEntity;
import com.ledger.ledgerservice.model.entity.DepartmentUserRole;
import com.ledger.ledgerservice.model.entity.Group;
import com.ledger.ledgerservice.model.entity.Permission;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.model.enums.UserStatus;
import com.ledger.ledgerservice.model.mapper.DepartmentUserRoleMapper;
import com.ledger.ledgerservice.model.mapper.PermissionMapper;
import com.ledger.ledgerservice.repository.DepartmentRepository;
import com.ledger.ledgerservice.repository.DepartmentUserRepository;
import com.ledger.ledgerservice.repository.DepartmentUserRoleRepository;
import com.ledger.ledgerservice.repository.GroupRepository;
import com.ledger.ledgerservice.repository.PermissionRepository;
import com.ledger.ledgerservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentUserRoleServiceImplTest {
    @Mock
    private DepartmentUserRoleRepository departmentUserRoleRepository;
    @Mock
    private DepartmentUserRepository departmentUserRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private DepartmentUserRoleMapper departmentUserRoleMapper;
    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private DepartmentUserRoleServiceImpl service;

    @Test
    void assignSuccessWithNewMembership() {
        DepartmentEntity department = DepartmentEntity.builder().id("dep-1").code("DEP_01").name("Department 1").status("ACTIVE").isActive(true).build();
        User user = User.builder().id("user-1").username("user1").status(UserStatus.ACTIVE).build();
        Group group = Group.builder().id("role-1").code("DEPT_ADMIN").name("Department admin").status(RecordStatus.ACTIVE).build();
        DepartmentUserEntity membership = DepartmentUserEntity.builder().id("du-1").departmentId("dep-1").userId("user-1").status(RecordStatus.ACTIVE).build();
        DepartmentUserRole entity = DepartmentUserRole.builder().id("dur-1").departmentUserId("du-1").groupId("role-1").status(RecordStatus.ACTIVE).build();
        DepartmentUserRoleResponse response = DepartmentUserRoleResponse.builder().id("dur-1").build();

        when(departmentRepository.findById("dep-1")).thenReturn(Optional.of(department));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(groupRepository.findByIdAndStatus("role-1", RecordStatus.ACTIVE)).thenReturn(Optional.of(group));
        when(departmentUserRepository.findByDepartmentIdAndUserId("dep-1", "user-1")).thenReturn(Optional.empty());
        when(departmentUserRepository.findByUserIdAndStatus("user-1", RecordStatus.ACTIVE)).thenReturn(List.of());
        when(departmentUserRepository.save(any(DepartmentUserEntity.class))).thenReturn(membership);
        when(departmentUserRoleRepository.findByDepartmentUserIdAndGroupId("du-1", "role-1")).thenReturn(Optional.empty());
        when(departmentUserRoleRepository.save(any(DepartmentUserRole.class))).thenReturn(entity);
        when(departmentUserRoleMapper.toResponse(entity)).thenReturn(response);

        DepartmentUserRoleResponse actual = service.assign(DepartmentUserRoleCreateRequest.builder()
                .departmentId("dep-1")
                .userId("user-1")
                .groupId("role-1")
                .build());

        assertEquals("dur-1", actual.getId());
        assertEquals("dep-1", actual.getDepartmentId());
        assertEquals("user-1", actual.getUserId());
        assertEquals("DEPT_ADMIN", actual.getGroupCode());
    }

    @Test
    void assignSuccessWithExistingMembership() {
        DepartmentEntity department = DepartmentEntity.builder().id("dep-1").code("DEP_01").name("Department 1").status("ACTIVE").isActive(true).build();
        User user = User.builder().id("user-1").username("user1").status(UserStatus.ACTIVE).build();
        Group group = Group.builder().id("role-1").code("DEPT_ADMIN").name("Department admin").status(RecordStatus.ACTIVE).build();
        DepartmentUserEntity membership = DepartmentUserEntity.builder().id("du-1").departmentId("dep-1").userId("user-1").status(RecordStatus.ACTIVE).build();
        DepartmentUserRole entity = DepartmentUserRole.builder().id("dur-1").departmentUserId("du-1").groupId("role-1").status(RecordStatus.ACTIVE).build();
        DepartmentUserRoleResponse response = DepartmentUserRoleResponse.builder().id("dur-1").build();

        when(departmentRepository.findById("dep-1")).thenReturn(Optional.of(department));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(groupRepository.findByIdAndStatus("role-1", RecordStatus.ACTIVE)).thenReturn(Optional.of(group));
        when(departmentUserRepository.findByDepartmentIdAndUserId("dep-1", "user-1")).thenReturn(Optional.of(membership));
        when(departmentUserRoleRepository.findByDepartmentUserIdAndGroupId("du-1", "role-1")).thenReturn(Optional.empty());
        when(departmentUserRoleRepository.save(any(DepartmentUserRole.class))).thenReturn(entity);
        when(departmentUserRoleMapper.toResponse(entity)).thenReturn(response);

        DepartmentUserRoleResponse actual = service.assign(DepartmentUserRoleCreateRequest.builder()
                .departmentId("dep-1")
                .userId("user-1")
                .groupId("role-1")
                .build());

        assertEquals("dur-1", actual.getId());
        assertEquals("dep-1", actual.getDepartmentId());
        assertEquals("user-1", actual.getUserId());
    }

    @Test
    void assignDepartmentNotFoundThrows() {
        when(departmentRepository.findById("dep-unknown")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.assign(DepartmentUserRoleCreateRequest.builder()
                        .departmentId("dep-unknown")
                        .userId("user-1")
                        .groupId("role-1")
                        .build()));

        assertEquals(MessageCode.NOT_FOUND.getCode(), exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void assignUserNotFoundThrows() {
        DepartmentEntity department = DepartmentEntity.builder().id("dep-1").status("ACTIVE").isActive(true).build();
        when(departmentRepository.findById("dep-1")).thenReturn(Optional.of(department));
        when(userRepository.findById("user-unknown")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.assign(DepartmentUserRoleCreateRequest.builder()
                        .departmentId("dep-1")
                        .userId("user-unknown")
                        .groupId("role-1")
                        .build()));

        assertEquals(MessageCode.USER_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void assignRoleNotFoundThrows() {
        DepartmentEntity department = DepartmentEntity.builder().id("dep-1").status("ACTIVE").isActive(true).build();
        User user = User.builder().id("user-1").status(UserStatus.ACTIVE).build();
        when(departmentRepository.findById("dep-1")).thenReturn(Optional.of(department));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(groupRepository.findByIdAndStatus("role-unknown", RecordStatus.ACTIVE)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.assign(DepartmentUserRoleCreateRequest.builder()
                        .departmentId("dep-1")
                        .userId("user-1")
                        .groupId("role-unknown")
                        .build()));

        assertEquals(MessageCode.GROUP_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void assignDuplicateActiveRoleThrows() {
        DepartmentEntity department = DepartmentEntity.builder().id("dep-1").status("ACTIVE").isActive(true).build();
        User user = User.builder().id("user-1").status(UserStatus.ACTIVE).build();
        Group group = Group.builder().id("role-1").status(RecordStatus.ACTIVE).build();
        DepartmentUserEntity membership = DepartmentUserEntity.builder().id("du-1").departmentId("dep-1").userId("user-1").status(RecordStatus.ACTIVE).build();
        DepartmentUserRole existingRole = DepartmentUserRole.builder().id("dur-1").departmentUserId("du-1").groupId("role-1").status(RecordStatus.ACTIVE).build();

        when(departmentRepository.findById("dep-1")).thenReturn(Optional.of(department));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(groupRepository.findByIdAndStatus("role-1", RecordStatus.ACTIVE)).thenReturn(Optional.of(group));
        when(departmentUserRepository.findByDepartmentIdAndUserId("dep-1", "user-1")).thenReturn(Optional.of(membership));
        when(departmentUserRoleRepository.findByDepartmentUserIdAndGroupId("du-1", "role-1")).thenReturn(Optional.of(existingRole));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.assign(DepartmentUserRoleCreateRequest.builder()
                        .departmentId("dep-1")
                        .userId("user-1")
                        .groupId("role-1")
                        .build()));

        assertEquals(MessageCode.DEPARTMENT_USER_ROLE_EXISTS.getCode(), exception.getCode());
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void assignReactivatesInactiveRole() {
        DepartmentEntity department = DepartmentEntity.builder().id("dep-1").status("ACTIVE").isActive(true).build();
        User user = User.builder().id("user-1").status(UserStatus.ACTIVE).build();
        Group group = Group.builder().id("role-1").code("ROLE_1").name("Role 1").status(RecordStatus.ACTIVE).build();
        DepartmentUserEntity membership = DepartmentUserEntity.builder().id("du-1").departmentId("dep-1").userId("user-1").status(RecordStatus.ACTIVE).build();
        DepartmentUserRole existingInactiveRole = DepartmentUserRole.builder().id("dur-1").departmentUserId("du-1").groupId("role-1").status(RecordStatus.INACTIVE).build();
        DepartmentUserRoleResponse response = DepartmentUserRoleResponse.builder().id("dur-1").build();

        when(departmentRepository.findById("dep-1")).thenReturn(Optional.of(department));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(groupRepository.findByIdAndStatus("role-1", RecordStatus.ACTIVE)).thenReturn(Optional.of(group));
        when(departmentUserRepository.findByDepartmentIdAndUserId("dep-1", "user-1")).thenReturn(Optional.of(membership));
        when(departmentUserRoleRepository.findByDepartmentUserIdAndGroupId("du-1", "role-1")).thenReturn(Optional.of(existingInactiveRole));
        when(departmentUserRoleRepository.save(existingInactiveRole)).thenReturn(existingInactiveRole);
        when(departmentUserRoleMapper.toResponse(existingInactiveRole)).thenReturn(response);

        DepartmentUserRoleResponse actual = service.assign(DepartmentUserRoleCreateRequest.builder()
                .departmentId("dep-1")
                .userId("user-1")
                .groupId("role-1")
                .build());

        assertEquals("dur-1", actual.getId());
        assertEquals(RecordStatus.ACTIVE, existingInactiveRole.getStatus());
    }

    @Test
    void revokeSuccess() {
        DepartmentUserRole entity = DepartmentUserRole.builder().id("dur-1").build();
        when(departmentUserRoleRepository.findById("dur-1")).thenReturn(Optional.of(entity));

        service.revoke("dur-1");

        verify(departmentUserRoleRepository).delete(entity);
    }

    @Test
    void getPermissionsByUserAndDepartmentResolvesPermissions() {
        DepartmentUserEntity membership = DepartmentUserEntity.builder().id("du-1").departmentId("dep-1").userId("user-1").status(RecordStatus.ACTIVE).build();
        Permission permission = Permission.builder().id("permission-1").code("INVOICE_APPROVE").status(RecordStatus.ACTIVE).build();
        PermissionResponse response = PermissionResponse.builder().id("permission-1").code("INVOICE_APPROVE").build();
        when(departmentUserRepository.findByDepartmentIdAndUserIdAndStatus("dep-1", "user-1", RecordStatus.ACTIVE)).thenReturn(Optional.of(membership));
        when(permissionRepository.findActiveDepartmentPermissions("user-1", "dep-1")).thenReturn(List.of(permission));
        when(permissionMapper.toResponses(List.of(permission))).thenReturn(List.of(response));

        List<PermissionResponse> actual = service.getPermissionsByUserAndDepartment("user-1", "dep-1");

        assertEquals(1, actual.size());
        assertEquals("INVOICE_APPROVE", actual.get(0).getCode());
    }
}
