package com.pos.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Adds per-request context to MDC so every log line carries:
 *   [requestId]  — short UUID for correlation across log lines
 *   [user]       — authenticated username or "anonymous"
 *
 * Also logs request start (DEBUG) and completion (INFO with duration).
 */
@Slf4j
@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "requestId";
    private static final String USERNAME    = "user";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest  request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain         chain) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long   startMs   = System.currentTimeMillis();

        MDC.put(REQUEST_ID, requestId);
        MDC.put(USERNAME, resolveUsername());

        try {
            log.debug(">>> {} {}", request.getMethod(), request.getRequestURI());
            chain.doFilter(request, response);
        } finally {
            // Re-resolve after filter chain runs (JWT filter may have set the principal by now)
            MDC.put(USERNAME, resolveUsername());

            long duration = System.currentTimeMillis() - startMs;
            log.info("<<< {} {} → {} ({}ms)",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration);

            MDC.remove(REQUEST_ID);
            MDC.remove(USERNAME);
        }
    }

    private String resolveUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "anonymous";
    }
}
