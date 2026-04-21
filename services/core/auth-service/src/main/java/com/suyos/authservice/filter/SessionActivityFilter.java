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

    /** Service for session management */
    private final SessionService sessionService;

    /** Utility for finding client IP address */
    private final ClientIpResolver clientIpResolver;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth && jwtAuth.isAuthenticated()) {

            Jwt jwt = jwtAuth.getToken();
            String sessionIdClaim = jwt.getClaimAsString("sessionId");

            if (sessionIdClaim != null) {
                UUID sessionId = UUID.fromString(sessionIdClaim);
                String ipAddress = clientIpResolver.extractClientIp(request);

                sessionService.updateLastActivity(sessionId, ipAddress);
            }
        }

        filterChain.doFilter(request, response);
    }

}