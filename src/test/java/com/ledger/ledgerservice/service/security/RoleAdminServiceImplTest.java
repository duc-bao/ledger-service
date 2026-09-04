package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.model.dto.request.UpdateRoleRequest;
import com.ledger.ledgerservice.model.dto.response.RoleResponse;
import com.ledger.ledgerservice.model.entity.Group;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.repository.DepartmentUserRoleRepository;
import com.ledger.ledgerservice.repository.CompanyUserRoleRepository;
import com.ledger.ledgerservice.repository.GroupRepository;
import com.ledger.ledgerservice.repository.RolePermissionRepository;
import com.ledger.ledgerservice.repository.UserGroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleAdminServiceImplTest {
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private UserGroupRepository userGroupRepository;
    @Mock
    private DepartmentUserRoleRepository departmentUserRoleRepository;
    @Mock
    private CompanyUserRoleRepository companyUserRoleRepository;
    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @InjectMocks
    private RoleAdminServiceImpl service;

    @Test
    void searchRolesReturnsPagedActiveRoles() {
        Group role = Group.builder().id("role-1").code("ACCOUNTANT").name("Accountant").status(RecordStatus.ACTIVE).sortOrder(1).build();
        when(groupRepository.searchRoles("account", RecordStatus.ACTIVE, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(role), PageRequest.of(0, 20), 1));

        Page<RoleResponse> page = service.searchRoles("account", RecordStatus.ACTIVE, PageRequest.of(0, 20));

        assertEquals(1, page.getTotalElements());
        assertEquals("ACCOUNTANT", page.getContent().getFirst().getCode());
    }

    @Test
    void updateRoleChangesEditableFields() {
        Group role = Group.builder().id("role-1").code("OLD").name("Old").description("Old desc").status(RecordStatus.ACTIVE).sortOrder(1).build();
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setCode("NEW_CODE");
        request.setName("New name");
        request.setDescription("New desc");
        request.setSortOrder(2);
        when(groupRepository.findByIdAndStatus("role-1", RecordStatus.ACTIVE)).thenReturn(Optional.of(role));
        when(groupRepository.existsByCodeIgnoreCaseAndIdNot("NEW_CODE", "role-1")).thenReturn(false);
        when(groupRepository.existsByNameIgnoreCaseAndIdNot("New name", "role-1")).thenReturn(false);
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoleResponse response = service.updateRole("role-1", request);

        assertEquals("NEW_CODE", response.getCode());
        assertEquals("New name", response.getName());
        assertEquals("New desc", response.getDescription());
        assertEquals(2, response.getSortOrder());
    }
}
