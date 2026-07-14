package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.exception.BusinessException;
import com.ledger.ledgerservice.model.dto.request.DepartmentUserRoleCreateRequest;
import com.ledger.ledgerservice.model.dto.response.DepartmentUserRoleResponse;
import com.ledger.ledgerservice.model.dto.response.PermissionResponse;
import com.ledger.ledgerservice.model.entity.DepartmentUserEntity;
import com.ledger.ledgerservice.model.entity.DepartmentUserRole;
import com.ledger.ledgerservice.model.entity.Group;
import com.ledger.ledgerservice.model.entity.Permission;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.model.mapper.DepartmentUserRoleMapper;
import com.ledger.ledgerservice.model.mapper.PermissionMapper;
import com.ledger.ledgerservice.repository.DepartmentUserRepository;
import com.ledger.ledgerservice.repository.DepartmentUserRoleRepository;
import com.ledger.ledgerservice.repository.GroupRepository;
import com.ledger.ledgerservice.repository.PermissionRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentUserRoleServiceImplTest {
    @Mock
    private DepartmentUserRoleRepository departmentUserRoleRepository;
    @Mock
    private DepartmentUserRepository departmentUserRepository;
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
    void assignSuccess() {
        DepartmentUserEntity membership = DepartmentUserEntity.builder().id("du-1").departmentId("dep-1").userId("user-1").status(RecordStatus.ACTIVE).build();
        Group group = Group.builder().id("role-1").code("DEPT_ADMIN").name("Department admin").status(RecordStatus.ACTIVE).build();
        DepartmentUserRole entity = DepartmentUserRole.builder().id("dur-1").departmentUserId("du-1").groupId("role-1").status(RecordStatus.ACTIVE).build();
        DepartmentUserRoleResponse response = DepartmentUserRoleResponse.builder().id("dur-1").build();

        when(departmentUserRepository.findByIdAndStatus("du-1", RecordStatus.ACTIVE)).thenReturn(Optional.of(membership));
        when(groupRepository.findByIdAndStatus("role-1", RecordStatus.ACTIVE)).thenReturn(Optional.of(group));
        when(departmentUserRoleRepository.existsByDepartmentUserIdAndGroupId("du-1", "role-1")).thenReturn(false);
        when(departmentUserRoleRepository.save(any(DepartmentUserRole.class))).thenReturn(entity);
        when(departmentUserRoleMapper.toResponse(entity)).thenReturn(response);

        DepartmentUserRoleResponse actual = service.assign(DepartmentUserRoleCreateRequest.builder().departmentUserId("du-1").groupId("role-1").build());

        assertEquals("dur-1", actual.getId());
        assertEquals("dep-1", actual.getDepartmentId());
        assertEquals("user-1", actual.getUserId());
        assertEquals("DEPT_ADMIN", actual.getGroupCode());
    }

    @Test
    void assignMembershipNotFoundThrows() {
        when(departmentUserRepository.findByIdAndStatus("du-1", RecordStatus.ACTIVE)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.assign(DepartmentUserRoleCreateRequest.builder().departmentUserId("du-1").groupId("role-1").build()));

        assertEquals(MessageCode.DEPARTMENT_USER_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void assignDuplicateThrows() {
        DepartmentUserEntity membership = DepartmentUserEntity.builder().id("du-1").status(RecordStatus.ACTIVE).build();
        Group group = Group.builder().id("role-1").status(RecordStatus.ACTIVE).build();
        when(departmentUserRepository.findByIdAndStatus("du-1", RecordStatus.ACTIVE)).thenReturn(Optional.of(membership));
        when(groupRepository.findByIdAndStatus("role-1", RecordStatus.ACTIVE)).thenReturn(Optional.of(group));
        when(departmentUserRoleRepository.existsByDepartmentUserIdAndGroupId("du-1", "role-1")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.assign(DepartmentUserRoleCreateRequest.builder().departmentUserId("du-1").groupId("role-1").build()));

        assertEquals(MessageCode.DEPARTMENT_USER_ROLE_EXISTS.getCode(), exception.getCode());
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
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
