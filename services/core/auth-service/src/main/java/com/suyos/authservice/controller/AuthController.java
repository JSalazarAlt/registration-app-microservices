package com.suyos.authservice.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.authservice.dto.internal.AuthenticationTokens;
import com.suyos.authservice.dto.request.AuthenticationRequest;
import com.suyos.authservice.dto.request.RegistrationRequest;
import com.suyos.authservice.dto.request.EmailResendRequest;
import com.suyos.authservice.dto.request.EmailVerificationRequest;
import com.suyos.authservice.dto.request.OAuth2AuthenticationRequest;
import com.suyos.authservice.dto.request.PasswordForgotRequest;
import com.suyos.authservice.dto.request.PasswordResetRequest;
import com.suyos.authservice.dto.request.RefreshTokenRequest;
import com.suyos.authservice.dto.response.AccountInfoResponse;
import com.suyos.authservice.dto.response.GenericMessageResponse;
import com.suyos.authservice.dto.response.MobileAuthenticationResponse;
import com.suyos.authservice.dto.response.WebAuthenticationResponse;
import com.suyos.authservice.service.AuthService;
import com.suyos.authservice.service.PasswordService;
import com.suyos.authservice.service.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for authentication operations.
 *
 * <p>Handles account registration and login endpoints and provides JWT-based
 * authentication for the application.</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
    name = "Authentication Management", 
    description = "Operations for managing authentication"
)
public class AuthController {
    
    /** Service for authentication business logic */
    private final AuthService authService;

    /** Service for password management */
    private final PasswordService passwordService;
    
    /** Service for token management */
    private final TokenService tokenService;

    /** Refresh token lifetime in days */
    private static final Long REFRESH_TOKEN_LIFETIME_DAYS = 30L;

    // ----------------------------------------------------------------
    // TRADITIONAL REGISTRATION AND LOGIN
    // ----------------------------------------------------------------

    /**
     * Registers a new user account.
     * 
     * @param request Account's information and user's profile
     * @return Created account's information with "201 Created" status
     */
    @PostMapping("/register")
    @Operation(
        summary = "Register account", 
        description = "Creates a new account with the provided registration data",
        responses = {
            @ApiResponse(
                responseCode = "201", description = "Registration successful",
                content = @Content(schema = @Schema(implementation = AccountInfoResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "409", description = "Email or username already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<AccountInfoResponse> registerAccount(
        @Valid @RequestBody RegistrationRequest request,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        // Create new account using registration data
        AccountInfoResponse accountInfo = authService.createAccount(request, idempotencyKey);
        
        // Return created account's information with "201 Created" status
        return ResponseEntity.status(HttpStatus.CREATED).body(accountInfo);
    }

    /**
     * Authenticates an account and returns refresh and access tokens.
     * 
     * @param request Account's credentials
     * @param httpRequest Account's credentials
     * @return Refresh and access tokens with "200 OK" status
     */
    @PostMapping("/login/web")
    @Operation(
        summary = "Web login account", 
        description = "Authenticates an account using its credentials for a web session",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Login successful", 
                content = @Content(schema = @Schema(implementation = AuthenticationTokens.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Account disabled or unverified"),
            @ApiResponse(responseCode = "423", description = "Account locked"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<WebAuthenticationResponse> webLoginAccount(
        @Valid @RequestBody AuthenticationRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {
        // Authenticate account using traditional login credentials
        AuthenticationTokens tokens = authService.authenticateAccount(request, httpRequest);

        // Build HttpOnly secure cookie for refresh token
        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/auth/refresh")
                .maxAge(Duration.ofDays(REFRESH_TOKEN_LIFETIME_DAYS))
                .build();

        // Add refresh token cookie to response headers
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // Build web authentication response without refresh token
        WebAuthenticationResponse webResponse = WebAuthenticationResponse.builder()
                .accountId(tokens.getAccountId())
                .accessToken(tokens.getAccessToken())
                .accessTokenExpiresIn(tokens.getAccessTokenExpiresIn())
                .build();
        
        // Return access token as JSON and refresh token as HttpOnly cookie with "200 OK" status 
        return ResponseEntity.ok(webResponse);
    }

     /**
     * Authenticates an account and returns refresh and access tokens.
     * 
     * @param request Account's credentials
     * @param httpRequest Account's credentials
     * @return Refresh and access tokens with "200 OK" status
     */
    @PostMapping("/login/mobile")
    @Operation(
        summary = "Mobile login account", 
        description = "Authenticates an account using its credentials for a mobile session",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Login successful", 
                content = @Content(schema = @Schema(implementation = AuthenticationTokens.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Account disabled or unverified"),
            @ApiResponse(responseCode = "423", description = "Account locked"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<MobileAuthenticationResponse> mobileLoginAccount(
        @Valid @RequestBody AuthenticationRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {
        // Authenticate account using traditional login credentials
        AuthenticationTokens tokens = authService.authenticateAccount(request, httpRequest);

        // Build web authentication response with refresh token
        MobileAuthenticationResponse mobileResponse = MobileAuthenticationResponse.builder()
                .accountId(tokens.getAccountId())
                .accessToken(tokens.getAccessToken())
                .accessTokenExpiresIn(tokens.getAccessTokenExpiresIn())
                .refreshToken(tokens.getRefreshToken())
                .build();
        
        // Return refresh and access tokens with "200 OK" status
        return ResponseEntity.ok(mobileResponse);
    }
    
    // ----------------------------------------------------------------
    // GOOGLE OAUTH2 REGISTRATION AND LOGIN
    // ----------------------------------------------------------------

    /**
     * Authenticates an account using Google OAuth2 credentials and returns
     * refresh and access tokens.
     *
     * @param request Account's information and user's profile from Google
     * @return Refresh and access tokens with "200 OK" status
     */
    @PostMapping("/oauth2/google")
    @Operation(
        summary = "Google OAuth2 register and login account",
        description = "Authenticates an account using Google OAuth2 credentials",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Login successful",
                content = @Content(schema = @Schema(implementation = AuthenticationTokens.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "403", description = "Account disabled or unverified"),
            @ApiResponse(responseCode = "423", description = "Account locked"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<AuthenticationTokens> googleOAuth2Authentication(
        @Valid @RequestBody OAuth2AuthenticationRequest request,
        HttpServletRequest httpRequest
    ) {
        // Authenticate account using Google OAuth2 credentials
        AuthenticationTokens response = authService.processGoogleOAuth2Account(request, httpRequest);

        // Return refresh and access tokens with "200 OK" status
        return ResponseEntity.ok(response);
    }

    // ----------------------------------------------------------------
    // LOGOUT
    // ----------------------------------------------------------------

    /**
     * Deauthenticates an account and revokes the refresh token.
     * 
     * @param request Refresh token value linked to account
     * @return logout response with "204 No Content" status
     */
    @PostMapping("/logout")
    @Operation(
        summary = "Logout account",
        description = "Deauthenticates an account from a single session and revokes refresh and access tokens",
        responses = {
            @ApiResponse(responseCode = "204", description = "Logout successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", description = "Refresh token revoked or expired"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<Void> logoutAccount(@RequestBody RefreshTokenRequest request) {
        // Deauthenticate account and revoke refresh token
        authService.deauthenticateAccount(request);
        
        // Return logout response with "204 No Content" status
        return ResponseEntity.noContent().build();
    }

    /**
     * Deauthenticates an account and revokes the refresh token.
     * 
     * @param request Refresh token value linked to account
     * @return logout response with "204 No Content" status
     */
    @PostMapping("/global-logout")
    @Operation(
        summary = "Globally logout account",
        description = "Deauthenticates an account from all sessions and revokes all refresh and access tokens",
        responses = {
            @ApiResponse(responseCode = "204", description = "Logout successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", description = "Refresh token revoked or expired"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<Void> globalLogoutAccount(@RequestBody RefreshTokenRequest request) {
        // Globally deauthenticate account and revoke refresh token
        authService.globalDeauthenticateAccount(request);
        
        // Return logout response with "204 No Content" status
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // TOKEN REFRESH
    // ----------------------------------------------------------------

    /**
     * Refreshes an access token using a refresh token and rotates the refresh
     * token.
     * 
     * @param request Refresh token value linked to account
     * @return New refresh and access tokens with "200 OK" status
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh access token",
        description = "Issues new JWT access token using valid refresh token",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Token refreshed successfully",
                content = @Content(schema = @Schema(implementation = AuthenticationTokens.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", description = "Refresh token revoked or expired"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<AuthenticationTokens> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        // Refresh access token using refresh token and rotate refresh token
        AuthenticationTokens response = tokenService.refreshToken(request);
        
        // Return new refresh and access tokens with "200 OK" status
        return ResponseEntity.ok(response);
    }

    // ----------------------------------------------------------------
    // EMAIL MANAGEMENT
    // ----------------------------------------------------------------

    /**
     * Verifies an email address and revokes used email verification token.
     * 
     * @param request Email verification token value linked to account
     * @return Account's information with "200 OK" status
     */
    @PostMapping("/verify-email")
    @Operation(
        summary = "Verify email",
        description = "Verifies the email address of an account",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Verify email successful", 
                content = @Content(schema = @Schema(implementation = AccountInfoResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "410", description = "Email verification token revoked or expired"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<AccountInfoResponse> verifyEmail(@Valid @RequestBody EmailVerificationRequest request) {
        // Authenticate an email using email verification token
        AccountInfoResponse accountInfo = authService.verifyEmail(request);

        // Return account's information with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }

    /**
     * Resends an email verification link to the associated account and revokes
     * old email verification tokens.
     * 
     * @param request Email address to send email verification link
     * @return Message of verification link sent with "200 OK" status
     */
    @PostMapping("/resend-verification")
    @Operation(
        summary = "Resend verification email",
        description = "Resends the verification link to the email address of an account",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Verify email successful",
                content = @Content(schema = @Schema(implementation = GenericMessageResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<GenericMessageResponse> resendEmailVerification(@Valid @RequestBody EmailResendRequest request) {
        // Resend verification link to authenticate an email
        GenericMessageResponse response = authService.resendEmailVerification(request);

        // Return message of verification link sent with "200 OK" status
        return ResponseEntity.ok(response);
    }
    
    // ----------------------------------------------------------------
    // PASSWORD MANAGEMENT
    // ----------------------------------------------------------------

    /**
     * Sends a password reset link to the associated account and revokes old
     * password reset tokens.
     *
     * @param request Email address to send password reset link
     * @return Message of password reset link sent with "200 OK" status
     */
    @PostMapping("/forgot-password")
    @Operation(
        summary = "Send password reset email",
        description = "Sends the password reset link to the email address of an account",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Password reset email sent",
                content = @Content(schema = @Schema(implementation = GenericMessageResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<GenericMessageResponse> forgotPassword(@Valid @RequestBody PasswordForgotRequest request) {
        // Send password reset link to associated account and revoke old 
        // password reset tokens
        GenericMessageResponse response = passwordService.forgotPassword(request);

        // Return message of password reset link sent with "200 OK" status
        return ResponseEntity.ok(response);
    }

    /**
     * Resets an account's password using a valid password reset token.
     *
     * @param request Password reset token value and new password
     * @return Account's information with "200 OK" status
     */
    @PostMapping("/reset-password")
    @Operation(
        summary = "Reset password",
        description = "Resets the password of an account",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Password reset email sent",
                content = @Content(schema = @Schema(implementation = AccountInfoResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "410", description = "Password reset token revoked or expired"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<AccountInfoResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        // Reset account's password using password reset token
        AccountInfoResponse accountInfo = passwordService.resetPassword(request);

        // Return account's information with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }
    
}