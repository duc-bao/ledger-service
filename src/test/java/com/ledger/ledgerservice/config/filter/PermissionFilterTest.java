package com.ledger.ledgerservice.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.ledgerservice.config.properties.AppSettingProperty;
import com.ledger.ledgerservice.model.context.RequestContext;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.model.security.JwtUserPrincipal;
import com.ledger.ledgerservice.service.security.PermissionResolutionService;
import com.ledger.ledgerservice.util.MessageHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionFilterTest {
    @Mock
    private PermissionResolutionService permissionResolutionService;
    @Mock
    private MessageHelper messageHelper;

    private PermissionFilter permissionFilter;

    @BeforeEach
    void setUp() {
        AppSettingProperty property = new AppSettingProperty();
        property.setSuperUser("superadmin");
        permissionFilter = new PermissionFilter(property, permissionResolutionService, new ObjectMapper().findAndRegisterModules(), messageHelper);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.clear();
    }

    @Test
    void publicEndpointPassesThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v3/api-docs/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        permissionFilter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        verify(permissionResolutionService, never()).hasApiPermission(anyString(), anyString(), anyString());
    }

    @Test
    void authorizedEndpointPassesThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/rbac/permissions");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new JwtUserPrincipal("user-1", "user1"), null, List.of()));
        RequestContextHolder.set(RequestContext.builder().requestId("req-1").spanId("req-1").username("user1").build());
        when(permissionResolutionService.hasApiPermission("user-1", "GET", "/api/v1/admin/rbac/permissions")).thenReturn(true);

        permissionFilter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertTrue(response.getContentAsString().isEmpty());
    }

    @Test
    void forbiddenEndpointReturns403() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/rbac/permissions");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new JwtUserPrincipal("user-1", "user1"), null, List.of()));
        RequestContextHolder.set(RequestContext.builder().requestId("req-1").spanId("req-1").username("user1").build());
        when(permissionResolutionService.hasApiPermission("user-1", "GET", "/api/v1/admin/rbac/permissions")).thenReturn(false);
        when(messageHelper.getMsg(anyString())).thenReturn("Forbidden");

        permissionFilter.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
        verify(permissionResolutionService).hasApiPermission("user-1", "GET", "/api/v1/admin/rbac/permissions");
    }
}

