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
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionFilter extends OncePerRequestFilter {
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
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
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
            log.warn("Authenticated request has no valid userId. Access denied for method={} URI={}", request.getMethod(), request.getRequestURI());
            forbidden(response, MessageCode.ACCESS_DENIED);
            return;
        }

        String username = resolveUsername(authentication);
        if (StringUtils.hasText(username) && username.equalsIgnoreCase(appSettingProperty.getSuperUser())) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean allowed;
        try {
            allowed = permissionResolutionService.hasApiPermission(userId, request.getMethod(), normalizeRequestUri(request));
        } catch (Exception ex) {
            log.error("Permission check failed for userId={}, method= {}, requestURI={}", userId, request.getMethod(), request.getRequestURI(), ex);
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
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String requestUri = normalizeRequestUri(request);

        return EXCLUDED_PATHS.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, requestUri));
    }

    private String normalizeRequestUri(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();

        if (StringUtils.hasText(contextPath)
                && requestUri.startsWith(contextPath)) {
            requestUri = requestUri.substring(contextPath.length());
        }

        if (!StringUtils.hasText(requestUri)) {
            return "/";
        }

        if (requestUri.length() > 1 && requestUri.endsWith("/")) {
            requestUri = requestUri.substring(0, requestUri.length() - 1);
        }

        return requestUri;
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
