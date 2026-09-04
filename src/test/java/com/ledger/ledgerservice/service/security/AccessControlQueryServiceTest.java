package com.ledger.ledgerservice.service.security;

import com.ledger.ledgerservice.config.properties.AppSettingProperty;
import com.ledger.ledgerservice.model.context.RequestContext;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.model.dto.response.AuthorizedMenuListResponse;
import com.ledger.ledgerservice.model.dto.response.AuthorizedMenuResponse;
import com.ledger.ledgerservice.model.entity.Group;
import com.ledger.ledgerservice.model.entity.Menu;
import com.ledger.ledgerservice.model.entity.User;
import com.ledger.ledgerservice.model.entity.UserGroup;
import com.ledger.ledgerservice.model.enums.MenuType;
import com.ledger.ledgerservice.model.enums.RecordStatus;
import com.ledger.ledgerservice.model.enums.UserStatus;
import com.ledger.ledgerservice.repository.GroupRepository;
import com.ledger.ledgerservice.repository.MenuRepository;
import com.ledger.ledgerservice.repository.UserGroupRepository;
import com.ledger.ledgerservice.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessControlQueryServiceTest {
    @Mock
    private MenuPermissionService menuPermissionService;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserGroupRepository userGroupRepository;
    @Mock
    private GroupRepository groupRepository;

    private AccessControlQueryService service;

    @BeforeEach
    void setUp() {
        AppSettingProperty property = new AppSettingProperty();
        property.setSuperUser("admin");
        service = new AccessControlQueryService(menuPermissionService, menuRepository, userRepository, userGroupRepository, groupRepository, property);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Test
    void getAuthorizedMenusForConfiguredSuperUserReturnsFullVisibleActiveMenuTree() {
        RequestContextHolder.set(RequestContext.builder().userId("admin-id").username("admin").build());
        when(menuRepository.findByStatusAndVisibleTrueOrderByOffsetAscCodeAsc(RecordStatus.ACTIVE))
                .thenReturn(List.of(rootMenu(), childMenu()));

        AuthorizedMenuListResponse response = service.getAuthorizedMenus();

        assertEquals(1, response.getMenus().size());
        assertEquals("DASHBOARD", response.getMenus().getFirst().getCode());
        assertEquals("REPORTS", response.getMenus().getFirst().getChildren().getFirst().getCode());
        verify(menuPermissionService, never()).getAuthorizedMenus("admin-id");
    }

    @Test
    void getAuthorizedMenusForRoleSuperAdminReturnsFullVisibleActiveMenuTree() {
        RequestContextHolder.set(RequestContext.builder().userId("user-id").username("boss").build());
        when(userRepository.findById("user-id")).thenReturn(Optional.of(User.builder().id("user-id").status(UserStatus.ACTIVE).build()));
        when(userGroupRepository.findActiveByUserIdAt(org.mockito.ArgumentMatchers.eq("user-id"), org.mockito.ArgumentMatchers.eq(RecordStatus.ACTIVE), org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of(UserGroup.builder().userId("user-id").groupId("role-id").status(RecordStatus.ACTIVE).build()));
        when(groupRepository.findById("role-id")).thenReturn(Optional.of(Group.builder().id("role-id").isSuperAdmin(true).status(RecordStatus.ACTIVE).build()));
        when(menuRepository.findByStatusAndVisibleTrueOrderByOffsetAscCodeAsc(RecordStatus.ACTIVE))
                .thenReturn(List.of(rootMenu()));

        AuthorizedMenuListResponse response = service.getAuthorizedMenus();

        assertEquals(1, response.getMenus().size());
        assertEquals("DASHBOARD", response.getMenus().getFirst().getCode());
        verify(menuPermissionService, never()).getAuthorizedMenus("user-id");
    }

    private Menu rootMenu() {
        Menu menu = Menu.builder()
                .id("menu-1")
                .code("DASHBOARD")
                .name("Dashboard")
                .path("/dashboard")
                .offset(1)
                .menuType(MenuType.MENU)
                .visible(true)
                .status(RecordStatus.ACTIVE)
                .ancestors(new ArrayList<>())
                .build();
        return menu;
    }

    private Menu childMenu() {
        Menu menu = Menu.builder()
                .id("menu-2")
                .code("REPORTS")
                .name("Reports")
                .path("/reports")
                .parentId("menu-1")
                .offset(2)
                .menuType(MenuType.MENU)
                .visible(true)
                .status(RecordStatus.ACTIVE)
                .ancestors(List.of("menu-1"))
                .build();
        return menu;
    }
}
