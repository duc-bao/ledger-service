package com.ledger.ledgerservice.service.user;

import com.ledger.ledgerservice.model.dto.request.AdminUpdateUserRequest;
import com.ledger.ledgerservice.model.dto.response.AdminUserDetailResponse;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.enums.UserStatus;
import com.ledger.ledgerservice.repository.DepartmentUserRepository;
import com.ledger.ledgerservice.repository.DepartmentUserRoleRepository;
import com.ledger.ledgerservice.repository.GroupRepository;
import com.ledger.ledgerservice.repository.UserGroupRepository;
import com.ledger.ledgerservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserGroupRepository userGroupRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private DepartmentUserRepository departmentUserRepository;
    @Mock
    private DepartmentUserRoleRepository departmentUserRoleRepository;

    @InjectMocks
    private UserAdminServiceImpl service;

    @Test
    void updateUserChangesEditableProfileFieldsAndTwoFactorFlag() {
        User user = User.builder().id("user-1").username("user1").email("old@example.com").phone("090").fullName("Old").status(UserStatus.ACTIVE).twoFactorEnabled(true).build();
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();
        request.setEmail("new@example.com");
        request.setPhone("091");
        request.setFullName("New Name");
        request.setUserType("INTERNAL");
        request.setRequireChange(true);
        request.setTwoFactorEnabled(false);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("091")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminUserDetailResponse response = service.updateUser("user-1", request);

        assertEquals("new@example.com", response.getEmail());
        assertEquals("091", response.getPhone());
        assertEquals("New Name", response.getFullName());
        assertEquals(false, response.getTwoFactorEnabled());
    }
}
