package com.chatapp.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Temporary diagnostic filter that logs the contents of the {@link SecurityContextHolder}
 * after the JWT authentication filter has run. This helps us understand why certain
 * endpoints still return 401 even though the JWT is valid.
 */
public class SecurityContextDebugFilter extends OncePerRequestFilter {

    public static final SecurityContextDebugFilter INSTANCE = new SecurityContextDebugFilter();

    private static final Logger log = LoggerFactory.getLogger(SecurityContextDebugFilter.class);

    private SecurityContextDebugFilter() {
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {
        Authentication before = SecurityContextHolder.getContext().getAuthentication();
        if (before == null) {
            log.debug("[SEC] Before chain - no authentication for URI {}", request.getRequestURI());
        } else {
            log.debug("[SEC] Before chain - auth type: {}, principal: {}, authenticated: {}",
                    before.getClass().getSimpleName(), before.getPrincipal(), before.isAuthenticated());
        }
        filterChain.doFilter(request, response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.warn("[SEC] After chain - no authentication for URI {}", request.getRequestURI());
        } else {
            log.debug("[SEC] After chain - auth type: {}, principal: {}, authenticated: {}",
                    authentication.getClass().getSimpleName(),
                    authentication.getPrincipal(),
                    authentication.isAuthenticated());
        }
    }
}