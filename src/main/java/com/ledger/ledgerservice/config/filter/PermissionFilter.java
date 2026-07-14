package com.ledger.ledgerservice.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.ledgerservice.config.properties.AppSettingProperty;
import com.ledger.ledgerservice.model.context.RequestContext;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.security.JwtUserPrincipal;
import com.ledger.ledgerservice.service.security.PermissionResolutionService;
import com.ledger.ledgerservice.util.MessageHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionFilter extends OncePerRequestFilter implements PermissionEvaluator {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/actuator/health",
            "/error",
            "/api/v1/login/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    );

    private final AppSettingProperty appSettingProperty;
    private final PermissionResolutionService permissionResolutionService;
    private final ObjectMapper objectMapper;
    private final MessageHelper messageHelper;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }
        String userId = resolveUserId(authentication);
        if (!StringUtils.hasText(userId)) {
            return false;
        }
        return permissionResolutionService.hasPermission(userId, String.valueOf(permission), null);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }
        String userId = resolveUserId(authentication);
        if (!StringUtils.hasText(userId)) {
            return false;
        }
        return permissionResolutionService.hasPermission(userId, String.valueOf(permission), targetType);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        RequestContext ctx = RequestContextHolder.get();
        if (ctx != null && StringUtils.hasText(ctx.getUsername()) && ctx.getUsername().equalsIgnoreCase(appSettingProperty.getSuperUser())) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = resolveUserId(authentication);
        if (!StringUtils.hasText(userId)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = resolveUsername(authentication);
        if (StringUtils.hasText(username) && username.equalsIgnoreCase(appSettingProperty.getSuperUser())) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean allowed;
        try {
            allowed = permissionResolutionService.hasApiPermission(userId, request.getMethod(), buildRequestUri(request));
        } catch (Exception ex) {
            log.error("Permission check failed for {} {}", request.getMethod(), request.getRequestURI(), ex);
            forbidden(response, MessageCode.ACCESS_DENIED);
            return;
        }

        if (!allowed) {
            forbidden(response, MessageCode.ACCESS_DENIED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || EXCLUDED_PATHS.stream().anyMatch(path -> PATH_MATCHER.match(path, request.getRequestURI()));
    }

    private String buildRequestUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (StringUtils.hasText(request.getQueryString())) {
            uri = uri + "?" + request.getQueryString();
        }
        return uri;
    }

    private String resolveUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
            return jwtUserPrincipal.userId();
        }
        return null;
    }

    private String resolveUsername(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
            return jwtUserPrincipal.username();
        }
        return principal != null ? String.valueOf(principal) : null;
    }

    private void forbidden(HttpServletResponse response, MessageCode code) throws IOException {
        String requestId = RequestContextHolder.getRequestId();
        BaseResponse<Object> body = BaseResponse.error(requestId, messageHelper.getMsg(code.getKey()), code.getCode(), HttpStatus.FORBIDDEN.value());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
