package com.suyos.authservice.config;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.suyos.authservice.service.JwtService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT authentication filter for processing JWT tokens in HTTP requests.
 *
 * <p>Intercepts incoming requests to extract and validate JWT tokens from
 * Authorization headers. Sets Spring Security context for valid tokens
 * and handles authentication for protected endpoints.</p>
 *
 * @author Joel Salazar
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Service for JWT token generation, validation, and extraction operations */
    private final JwtService jwtService;
    
    /** Service for loading user details from the database */
    private final UserDetailsService userDetailsService;
    
    /** Service for managing blacklisted JWT tokens */
    //private final TokenBlacklistService tokenBlacklistService;

    /**
     * Processes each HTTP request to extract, validate, and apply authentication 
     * based on JWT tokens.
     *
     * <p><b>Behavior:</b></p>
     * <ol>
     *   <li>Extracts the JWT token from the <code>Authorization</code> header 
     *       if present.</li>
     *   <li>Checks whether the token is blacklisted (e.g., after a logout) using 
     *       the {@code tokenBlacklistService}.</li>
     *   <li>Validates the token's integrity and extracts the associated user email 
     *       via the {@code jwtService}.</li>
     *   <li>Loads user details from the {@code UserDetailsService} and sets the 
     *       authentication in the {@link SecurityContextHolder} if the token is valid.</li>
     *   <li>Continues the filter chain by passing the request to the next filter 
     *       regardless of authentication outcome.</li>
     * </ol>
     *
     * <p><b>Purpose:</b></p>
     * <ul>
     *   <li>Authenticates incoming requests by verifying JWT tokens issued to 
     *       valid users.</li>
     *   <li>Prevents the use of expired, invalid, or blacklisted tokens to access 
     *       protected endpoints.</li>
     *   <li>Ensures that valid users are recognized by Spring Security and 
     *       granted the appropriate authorities.</li>
     *   <li>Maintains a stateless authentication model suitable for RESTful 
     *       APIs using JWT-based security.</li>
     * </ul>
     *
     * <hr>
     * 
     * @param request the HTTP request containing the Authorization header
     * @param response the HTTP response
     * @param filterChain the filter chain used to continue request processing
     * @throws ServletException if servlet processing fails
     * @throws IOException if an I/O operation fails
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
         // Get the Authorization header from the request
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Skip JWT processing if there is no Authorization header or it doesn't start with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token from Authorization header (remove "Bearer " prefix)
        jwt = authHeader.substring(7);
        
        try {
            // Check if the token is blacklisted (e.g., after logout)
            /*/
            if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                log.debug("Blacklisted token attempted to be used");
                filterChain.doFilter(request, response);
                return;
            }
            */

            // Extract the user email (username) from the JWT token
            userEmail = jwtService.extractUsername(jwt);

            // Proceed with validation if userEmail is found and no authentication is set in the context
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Load user details from the database
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                // Validate the JWT token against the user details (checks signature, expiration, etc.)
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Create an authenticated UsernamePasswordAuthenticationToken
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    // Attach request details (IP, session, etc.) to the authentication token
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // Set the authentication in the Spring Security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException e) {
            // JWT validation failed (invalid, expired, malformed, etc.)
            log.debug("JWT validation failed: {}", e.getMessage());
        } catch (Exception e) {
            // Any other unexpected error during JWT processing
            log.error("Unexpected error during JWT processing: {}", e.getMessage());
        }
        // Pass the request to the next filter in the chain (continue processing)
        filterChain.doFilter(request, response);
    }
    
}