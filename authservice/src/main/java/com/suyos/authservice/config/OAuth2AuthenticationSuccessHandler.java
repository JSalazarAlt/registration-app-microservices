package com.suyos.authservice.config;

import java.io.IOException;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.suyos.authservice.service.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles successful Google OAuth2 authentication.
 * 
 * Processes Google OAuth2 user information, creates or updates user,
 * generates JWT token, and redirects to frontend with token.
 * 
 * @author Joel Salazar
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
     * Handles successful Google OAuth2 authentication and redirects the user 
     * to the frontend with a generated access token.
     *
     * <p><b>Behavior:</b></p>
     * <ol>
     *   <li>Extracts user information (email, name, provider ID) from the 
     *       {@link OAuth2User} principal.</li>
     *   <li>Processes the authenticated user through 
     *       {@code authService.processGoogleOAuth2User()} to generate an access token.</li>
     *   <li>Builds a redirect URL including token and basic user details.</li>
     *   <li>Redirects the user to the frontend’s OAuth2 callback endpoint.</li>
     *   <li>Logs and redirects to the login page with an error flag if any 
     *       exception occurs.</li>
     * </ol>
     *
     * <p><b>Purpose:</b></p>
     * <ul>
     *   <li>Completes the Google OAuth2 login flow after successful authentication.</li>
     *   <li>Transfers user data and token securely to the frontend for session 
     *       initialization.</li>
     *   <li>Ensures proper error handling and user feedback on authentication 
     *       failure.</li>
     * </ul>
     *
     * <hr>
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param authentication the authenticated {@link Authentication} containing 
     *        OAuth2 user details
     * @throws IOException if an I/O operation fails
     * @throws ServletException if servlet processing fails
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
        try {
            // Extract Google user information
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String providerId = oauth2User.getAttribute("sub"); // Google uses 'sub'
            
            // Process Google OAuth2 user
            var authResponse = authService.processGoogleOAuth2Account(email, name, providerId);
            String token = authResponse.getAccessToken();
            
            // Redirect to frontend with token and account data
            String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth2/redirect")
                    .queryParam("accountId", authResponse.getAccountId())
                    .queryParam("token", token)
                    .queryParam("tokenType", authResponse.getTokenType())
                    .queryParam("expiresIn", authResponse.getExpiresIn())
                    .build().toUriString();
            
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
        } catch (Exception e) {
            log.error("Google OAuth2 authentication failed", e);
            getRedirectStrategy().sendRedirect(request, response, "http://localhost:5173/login?error=oauth2_failed");
        }
    }
    
}