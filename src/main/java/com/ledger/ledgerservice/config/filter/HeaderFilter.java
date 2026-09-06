package com.ledger.ledgerservice.config.filter;

import com.ledger.ledgerservice.model.constant.HeaderConstant;
import com.ledger.ledgerservice.model.context.RequestContext;
import com.ledger.ledgerservice.model.context.holder.RequestContextHolder;
import com.ledger.ledgerservice.service.audit.ActionLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class HeaderFilter extends OncePerRequestFilter {

    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,64}$");
    private static final Pattern SENSITIVE_QUERY_PATTERN =
            Pattern.compile("(?i)(password|token|secret|otp|access_token|refresh_token|credential)=([^&]*)");

    private final ActionLogService actionLogService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        LocalDateTime startAt = LocalDateTime.now();
        String requestId = resolveRequestId(request.getHeader(HeaderConstant.X_REQUEST_ID));

        RequestContext context = RequestContext.builder()
                .execute(true)
                .requestId(requestId)
                .spanId(requestId)
                .requestIp(resolveIp(request))
                .requestMethod(request.getMethod())
                .requestUrl(request.getRequestURL() != null ? request.getRequestURL().toString() : null)
                .requestQuery(sanitizeQueryString(request.getQueryString()))
                .requestUrlPath(request.getRequestURI())
                .userAgent(request.getHeader("User-Agent"))
                .service("ledger-service")
                .action(request.getMethod() + " " + request.getRequestURI())
                .requestStart(startAt)
                .build();

        RequestContextHolder.set(context);
        response.setHeader(HeaderConstant.X_REQUEST_ID, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            RequestContext ctx = RequestContextHolder.get();
            if (ctx != null) {
                LocalDateTime endAt = LocalDateTime.now();
                ctx.setRequestEnd(endAt);
                ctx.setStatusCode(response.getStatus());
                ctx.setDurationMs(Duration.between(startAt, endAt).toMillis());
                actionLogService.save(ctx);
            }
            RequestContextHolder.clear();
        }
    }

    private String resolveRequestId(String requestIdHeader) {
        if (StringUtils.hasText(requestIdHeader) && REQUEST_ID_PATTERN.matcher(requestIdHeader.trim()).matches()) {
            return requestIdHeader.trim();
        }
        return UUID.randomUUID().toString();
    }

    private String sanitizeQueryString(String queryString) {
        if (!StringUtils.hasText(queryString)) {
            return null;
        }
        return SENSITIVE_QUERY_PATTERN.matcher(queryString).replaceAll("$1=******");
    }

    private String resolveIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
