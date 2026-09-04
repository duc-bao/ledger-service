package com.ledger.ledgerservice.service.company;

import com.ledger.ledgerservice.model.dto.request.company.CompanyUserRoleCreateRequest;
import com.ledger.ledgerservice.model.dto.response.PermissionResponse;
import com.ledger.ledgerservice.model.dto.response.company.CompanyUserRoleResponse;
import com.ledger.ledgerservice.model.entity.Company;
import com.ledger.ledgerservice.model.entity.CompanyUserRole;
import com.ledger.ledgerservice.model.entity.Group;
import com.ledger.ledgerservice.model.entity.Permission;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.enums.CompanyStatus;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.model.enums.UserStatus;
import com.ledger.ledgerservice.repository.CompanyRepository;
import com.ledger.ledgerservice.repository.CompanyUserRoleRepository;
import com.ledger.ledgerservice.repository.GroupRepository;
import com.ledger.ledgerservice.repository.PermissionRepository;
import com.ledger.ledgerservice.repository.UserRepository;
import com.ledger.ledgerservice.model.mapper.PermissionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyUserRoleServiceImplTest {
    @Mock
    private CompanyUserRoleRepository companyUserRoleRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private CompanyUserRoleServiceImpl service;

    @Test
    void assignCreatesCompanyScopedRole() {
        CompanyUserRoleCreateRequest request = new CompanyUserRoleCreateRequest();
        request.setCompanyId("company-1");
        request.setUserId("user-1");
        request.setGroupId("role-1");
        when(companyRepository.findById("company-1")).thenReturn(Optional.of(company()));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(User.builder().id("user-1").status(UserStatus.ACTIVE).build()));
        when(groupRepository.findByIdAndStatus("role-1", RecordStatus.ACTIVE)).thenReturn(Optional.of(role()));
        when(companyUserRoleRepository.existsByCompanyIdAndUserIdAndGroupId("company-1", "user-1", "role-1")).thenReturn(false);
        when(companyUserRoleRepository.save(any(CompanyUserRole.class))).thenReturn(CompanyUserRole.builder().id("cur-1").companyId("company-1").userId("user-1").groupId("role-1").status(RecordStatus.ACTIVE).build());

        CompanyUserRoleResponse response = service.assign(request);

        assertEquals("cur-1", response.getId());
        assertEquals("company-1", response.getCompanyId());
        assertEquals("role-1", response.getGroupId());
    }

    @Test
    void getPermissionsReturnsRolePermissionsWithinCompanyScope() {
        Permission permission = Permission.builder().id("permission-1").code("COMPANY_VIEW").status(RecordStatus.ACTIVE).build();
        PermissionResponse response = PermissionResponse.builder().id("permission-1").code("COMPANY_VIEW").build();
        when(companyRepository.findById("company-1")).thenReturn(Optional.of(company()));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(User.builder().id("user-1").status(UserStatus.ACTIVE).build()));
        when(permissionRepository.findActiveCompanyPermissions("user-1", "company-1")).thenReturn(List.of(permission));
        when(permissionMapper.toResponses(List.of(permission))).thenReturn(List.of(response));

        List<PermissionResponse> permissions = service.getPermissions("company-1", "user-1");

        assertEquals(1, permissions.size());
        assertEquals("COMPANY_VIEW", permissions.getFirst().getCode());
    }

    private Company company() {
        Company company = new Company();
        company.setId("company-1");
        company.setStatus(CompanyStatus.ACTIVE);
        return company;
    }

    private Group role() {
        return Group.builder().id("role-1").code("ROLE").name("Role").status(RecordStatus.ACTIVE).build();
    }
}
