package com.suyos.authservice.filter;

import java.io.IOException;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.suyos.authservice.service.SessionService;
import com.suyos.authservice.util.ClientIpResolver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionActivityFilter extends OncePerRequestFilter {

    private final SessionService sessionService;

    private final ClientIpResolver clientIpResolver;

    /**
     * Intercepts each request to update last activity timestamp and client IP
     * address for authenticated sessions based on JWT information.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain filter chain
     */
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        // Extract authentication from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Process only authenticated JWT-based requests
        if (authentication instanceof JwtAuthenticationToken jwtAuth && jwtAuth.isAuthenticated()) {
            // Extract session ID from JWT claims
            Jwt jwt = jwtAuth.getToken();
            String sessionIdClaim = jwt.getClaimAsString("sid");

            // Update session activity if session ID is present
            if (sessionIdClaim != null) {
                UUID sessionId = UUID.fromString(sessionIdClaim);
                String ipAddress = clientIpResolver.extractClientIp(request);
                sessionService.updateLastActivity(sessionId, ipAddress);
            }
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }

}