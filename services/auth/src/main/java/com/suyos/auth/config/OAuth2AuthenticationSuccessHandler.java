package com.suyos.auth.config;

import java.io.IOException;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.suyos.auth.dto.request.OAuth2AuthenticationRequestDTO;
import com.suyos.auth.service.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles successful Google OAuth2 authentication.
 *
 * <p>Processes Google OAuth2 user information after successful
 * authentication, creates or links account, generates JWT tokens,
 * and redirects to frontend with authentication details.</p>
 *
 * <p>Integrates with AuthService for account management and token
 * generation. Handles errors gracefully by redirecting to login
 * page with error parameters.</p>
 */
@Component
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    /** Service for authentication operations */
    private final AuthService authService;
    
    public OAuth2AuthenticationSuccessHandler(@Lazy AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Handles successful OAuth2 authentication and redirects to frontend.
     *
     * <p><b>Behavior:</b></p>
     * <ul>
     *   <li>Extracts user info from OAuth2User principal</li>
     *   <li>Processes account creation/linking via AuthService</li>
     *   <li>Redirects to frontend with tokens and account data</li>
     *   <li>Redirects to login with error on failure</li>
     * </ul>
     *
     * <p><b>Purpose:</b></p>
     * <ul>
     *   <li>Completes OAuth2 flow after Google authentication</li>
     *   <li>Transfers tokens to frontend for session initialization</li>
     * </ul>
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param authentication OAuth2 authentication with user details
     * @throws IOException If redirect fails
     * @throws ServletException If servlet processing fails
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        // Extract OAuth2 user principal
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
        try {
            // Extract Google user information from OAuth2 attributes
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String providerId = oauth2User.getAttribute("sub");

            // Build OAuth2 authentication request DTO
            OAuth2AuthenticationRequestDTO dto = OAuth2AuthenticationRequestDTO.builder()
                    .email(email)
                    .name(name)
                    .providerId(providerId)
                    .build();
            
            // Process OAuth2 account (create or link) and generate tokens
            var authResponse = authService.processGoogleOAuth2Account(dto);
            String token = authResponse.getAccessToken();
            
            // Build redirect URL with authentication data
            String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth2/redirect")
                    .queryParam("accountId", authResponse.getAccountId())
                    .queryParam("token", token)
                    .queryParam("tokenType", authResponse.getTokenType())
                    .queryParam("expiresIn", authResponse.getAccessTokenExpiresIn())
                    .build().toUriString();
            
            // Redirect to frontend OAuth2 callback
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
        } catch (Exception e) {
            // Log error and redirect to login with error flag
            log.error("Google OAuth2 authentication failed", e);
            getRedirectStrategy().sendRedirect(request, response, "http://localhost:5173/login?error=oauth2_failed");
        }
    }
    
}