package com.suyos.authservice.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
import com.suyos.authservice.dto.request.PasswordChangeRequest;
import com.suyos.authservice.dto.request.PasswordForgotRequest;
import com.suyos.authservice.dto.request.PasswordResetRequest;
import com.suyos.authservice.dto.request.RefreshTokenRequest;
import com.suyos.authservice.dto.response.AccountInfoResponse;
import com.suyos.authservice.dto.response.AuthenticationResponse;
import com.suyos.authservice.dto.response.GenericMessageResponse;
import com.suyos.authservice.service.AuthService;
import com.suyos.authservice.service.PasswordService;
import com.suyos.authservice.service.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
        @Parameter(description = "Account's information and user's profile") @Valid @RequestBody RegistrationRequest request,
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
    @PostMapping("/login")
    @Operation(
        summary = "Login account", 
        description = "Authenticates an account using its credentials",
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
    public ResponseEntity<AuthenticationResponse> loginAccount(
        @Parameter(description = "Account's credentials") @Valid @RequestBody AuthenticationRequest request,
        HttpServletRequest httpRequest
    ) {
        // Authenticate account using traditional login credentials
        AuthenticationTokens tokens = authService.authenticateAccount(request, httpRequest);

        // Build authentication response with refresh token
        AuthenticationResponse response = AuthenticationResponse.builder()
                .accountId(tokens.getAccountId())
                .accessToken(tokens.getAccessToken())
                .accessTokenExpiresIn(tokens.getAccessTokenExpiresIn())
                .refreshToken(tokens.getRefreshToken())
                .build();
        
        // Return refresh and access tokens with "200 OK" status
        return ResponseEntity.ok(response);
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
        @Parameter(description = "Account's information and user's profile from Google") @Valid @RequestBody OAuth2AuthenticationRequest request,
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
     * @return Logout response with "204 No Content" status
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
    public ResponseEntity<Void> logoutAccount(
        @Parameter(description = "Refresh token linked to account") @Valid @RequestBody RefreshTokenRequest request
    ) {
        // Deauthenticate account and revoke refresh token
        authService.deauthenticateAccount(request);
        
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
    public ResponseEntity<AuthenticationResponse> refreshToken(
        @Parameter(description = "Refresh token linked to account") @Valid @RequestBody RefreshTokenRequest request
    ) {
        // Refresh access token using refresh token and rotate refresh token
        AuthenticationTokens tokens = tokenService.refreshToken(request);

        // Build mobile authentication response with refresh token
        AuthenticationResponse response = AuthenticationResponse.builder()
                .accountId(tokens.getAccountId())
                .accessToken(tokens.getAccessToken())
                .accessTokenExpiresIn(tokens.getAccessTokenExpiresIn())
                .refreshToken(tokens.getRefreshToken())
                .build();
        
        // Return refresh and access tokens with "200 OK" status 
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
    @PostMapping("/email/verify")
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
    public ResponseEntity<AccountInfoResponse> verifyEmail(
        @Parameter(description = "Email verification token linked to account") @Valid @RequestBody EmailVerificationRequest request
    ) {
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
    @PostMapping("/email/resend-verification")
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
    public ResponseEntity<GenericMessageResponse> resendEmailVerification(
        @Parameter(description = "Email address to send email verification link") @Valid @RequestBody EmailResendRequest request
    ) {
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
    @PostMapping("/password/forgot")
    @Operation(
        summary = "Forgot password",
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
    public ResponseEntity<GenericMessageResponse> forgotPassword(
        @Parameter(description = "Email address to send password reset link") @Valid @RequestBody PasswordForgotRequest request
    ) {
        // Send password reset link to associated account and revoke old 
        // password reset tokens
        GenericMessageResponse response = passwordService.requestPasswordReset(request);

        // Return message of password reset link sent with "200 OK" status
        return ResponseEntity.ok(response);
    }

    /**
     * Resets an account's password using a valid password reset token.
     *
     * @param request Password reset token value and new password
     * @return Account's information with "200 OK" status
     */
    @PostMapping("/password/reset")
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
    public ResponseEntity<AccountInfoResponse> resetPassword(
        @Parameter(description = "Password reset token and new password") @Valid @RequestBody PasswordResetRequest request
    ) {
        // Reset account's password using password reset token
        AccountInfoResponse accountInfo = passwordService.confirmPasswordReset(request);

        // Return account's information with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }

    /**
     * Changes an account's password using its current password.
     *
     * @param request Current password and new password
     * @return Account's information with "200 OK" status
     */
    @PostMapping("/password/change")
    @Operation(
        summary = "Change password",
        description = "Changes the password of an account",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Password changed successfully",
                content = @Content(schema = @Schema(implementation = AccountInfoResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "410", description = "Password reset token revoked or expired"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<AccountInfoResponse> changePassword(
        @AuthenticationPrincipal Jwt jwt,
        @Parameter(description = "Current password and new password") @Valid @RequestBody PasswordChangeRequest request
    ) {
        // Extract authenticated account's ID from access token
        UUID authenticatedAccountId = UUID.fromString(jwt.getSubject());

        // Change account's password using current password
        AccountInfoResponse accountInfo = passwordService.changePassword(authenticatedAccountId, request);

        // Return account's information with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }
    
}