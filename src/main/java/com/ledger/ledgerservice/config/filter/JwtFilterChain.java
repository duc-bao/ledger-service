package com.ledger.ledgerservice.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.ledgerservice.config.properties.JwtProperties;
import com.ledger.ledgerservice.model.context.RequestContext;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.model.dto.response.BaseResponse;
import com.ledger.ledgerservice.model.constant.HeaderConstant;
import com.ledger.ledgerservice.model.enums.MessageCode;
import com.ledger.ledgerservice.model.security.JwtUserPrincipal;
import com.ledger.ledgerservice.service.auth.TokenBlacklistService;
import com.ledger.ledgerservice.util.JwtUtils;
import com.ledger.ledgerservice.util.MessageHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilterChain extends OncePerRequestFilter {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private final MessageHelper messageHelper;
    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;
    private final TokenBlacklistService tokenBlacklistService;
    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/actuator/health",
            "/error",
            "/api/v1/login/**"
    );

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader(HeaderConstant.AUTHORIZATION);
            if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(JwtUtils.TOKEN_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(JwtUtils.TOKEN_PREFIX.length()).trim();
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.error("Access token is blacklisted");
                unauthorized(response, MessageCode.UNAUTHORIZED);
                return;
            }
            String secret = resolveJwtSecret();
            if (!StringUtils.hasText(secret) || !JwtUtils.verified(token, secret) || JwtUtils.isExpired(token, secret)) {
                log.error("Cannot verify access token");
                unauthorized(response, MessageCode.UNAUTHORIZED);
                return;
            }

            Map<String, Object> claims = JwtUtils.getClaims(token, secret);
            String username = resolveUsername(claims);
            if (!StringUtils.hasText(username)) {
                log.error("Cannot resolve username from claims {}", claims);
                unauthorized(response, MessageCode.UNAUTHORIZED);
                return;
            }

            String userId = resolveUserId(claims);
            JwtUserPrincipal principal = new JwtUserPrincipal(userId, username);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(principal, null, List.of());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            RequestContext context = RequestContextHolder.get();
            if (context != null) {
                context.setUsername(username);
            }

            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            log.error("Cannot authenticate access token", ex);
            unauthorized(response, MessageCode.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String method = request.getMethod();
        String requestURI = request.getRequestURI();
        return "OPTIONS".equalsIgnoreCase(method) || EXCLUDED_PATHS.stream().anyMatch(path -> PATH_MATCHER.match(path, requestURI));
    }

    private String resolveJwtSecret() {
        if (StringUtils.hasText(jwtProperties.getSecret())) {
            return jwtProperties.getSecret();
        }
        return jwtProperties.getKey();
    }

    private String resolveUsername(Map<String, Object> claims) {
        if (claims == null) {
            return null;
        }
        Object username = claims.get("username");
        if (username != null) {
            return String.valueOf(username);
        }
        Object subject = claims.get("sub");
        return subject != null ? String.valueOf(subject) : null;
    }

    private String resolveUserId(Map<String, Object> claims) {
        if (claims == null) {
            return null;
        }
        Object userId = claims.get("userId");
        if (userId == null) {
            userId = claims.get("user_id");
        }
        if (userId == null) {
            userId = claims.get("uid");
        }
        return userId != null ? String.valueOf(userId) : null;
    }

    private void unauthorized(HttpServletResponse response, MessageCode code) throws IOException {
        HttpStatus status = code == MessageCode.SERVICE_UNAVAILABLE ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.UNAUTHORIZED;
        String requestId = RequestContextHolder.getRequestId();
        BaseResponse<Object> body = BaseResponse.error(
                requestId,
                messageHelper.getMsg(code.getKey()),
                code.getCode(),
                status.value()
        );

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
