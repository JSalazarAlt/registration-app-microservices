package com.suyos.authservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.authservice.dto.request.AuthenticationRequestDTO;
import com.suyos.authservice.dto.request.RegistrationRequestDTO;
import com.suyos.authservice.dto.request.EmailResendRequestDTO;
import com.suyos.authservice.dto.request.EmailVerificationRequestDTO;
import com.suyos.authservice.dto.request.OAuth2AuthenticationRequestDTO;
import com.suyos.authservice.dto.request.PasswordForgotRequestDTO;
import com.suyos.authservice.dto.request.PasswordResetRequestDTO;
import com.suyos.authservice.dto.request.RefreshTokenRequestDTO;
import com.suyos.authservice.dto.response.AccountInfoDTO;
import com.suyos.authservice.dto.response.AuthenticationResponseDTO;
import com.suyos.authservice.dto.response.GenericMessageResponseDTO;
import com.suyos.authservice.service.AuthService;
import com.suyos.authservice.service.PasswordService;
import com.suyos.authservice.service.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for authentication operations.
 *
 * <p>Handles account registration and login endpoints and provides JWT-based
 * authentication for the application.</p>
 *
 * @author Joel Salazar
 */
@RestController
@RequestMapping("/api/v1/auth")
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
     * @param request Registration data
     * @return ResponseEntity containing created account's information
     */
    @PostMapping("/register")
    @Operation(
        summary = "Register new account", 
        description = "Creates a new account with the provided information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Registration successful"),
        @ApiResponse(responseCode = "400", description = "Invalid registration data or email/username already exists")
    })
    public ResponseEntity<AccountInfoDTO> registerAccount(@Valid @RequestBody RegistrationRequestDTO request) {
        // Create a new account using the registration data
        AccountInfoDTO accountInfo = authService.createAccount(request);
        
        // Return the created account info with "201 Created" status
        return ResponseEntity.status(HttpStatus.CREATED).body(accountInfo);
    }

    /**
     * Authenticates account and returns refresh and access tokens.
     * 
     * @param request Login credentials
     * @return ResponseEntity containing refresh and access tokens
     */
    @PostMapping("/login")
    @Operation(
        summary = "Login account", 
        description = "Authenticates account credentials and returns refresh and access tokens"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful, tokens returned"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials or account not active")
    })
    public ResponseEntity<AuthenticationResponseDTO> loginAccount(@Valid @RequestBody AuthenticationRequestDTO request) {
        // Authenticate an account using the login credentials
        AuthenticationResponseDTO response = authService.authenticateAccount(request);
        
        // Return the authentication response with "200 OK" status
        return ResponseEntity.ok(response);
    }
    
    // ----------------------------------------------------------------
    // GOOGLE OAUTH2 REGISTRATION AND LOGIN
    // ----------------------------------------------------------------

    /**
     * Authenticates account using Google OAuth2 and returns refresh and access tokens.
     *
     * @param request Google OAuth2 authentication request
     * @return ResponseEntity containing refresh and access tokens
     */
    @PostMapping("/oauth2/google")
    @Operation(
        summary = "Google OAuth2 register and login account",
        description = "Authenticates account using Google OAuth2 and returns refresh and access tokens"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful, tokens returned"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials or account not active")
    })
    public ResponseEntity<AuthenticationResponseDTO> googleOAuth2Authentication(@Valid @RequestBody OAuth2AuthenticationRequestDTO request) {
        // Authenticate an account using the Google OAuth2 credentials
        AuthenticationResponseDTO response = authService.processGoogleOAuth2Account(request);

        // Return the authentication response with "200 OK" status
        return ResponseEntity.ok(response);
    }

    // ----------------------------------------------------------------
    // LOGOUT
    // ----------------------------------------------------------------

    /**
     * Deauthenticates account and revokes refresh token.
     * 
     * @param request Refresh token request value
     * @return ResponseEntity indicating logout success
     */
    @PostMapping("/logout")
    @Operation(
        summary = "Logout account",
        description = "Invalidates account's refresh token and JWT access token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Logout successful"),
        @ApiResponse(responseCode = "400", description = "Invalid refresh token")
    })
    public ResponseEntity<Void> logoutAccount(@RequestBody RefreshTokenRequestDTO request) {
        // Deauthenticate an account revoking the refresh token
        authService.deauthenticateAccount(request);
        
        // Return the logout response with "204 No Content" status
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // TOKEN REFRESH
    // ----------------------------------------------------------------

    /**
     * Refreshes access token using the refresh token.
     * 
     * @param request Refresh token request
     * @return ResponseEntity containing new refresh and access tokens
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh JWT token",
        description = "Generates new JWT token using valid refresh token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    public ResponseEntity<AuthenticationResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        // Refresh access token using the refresh token
        AuthenticationResponseDTO response = tokenService.refreshToken(request);
        
        // Return the authentication response with "200 OK" status
        return ResponseEntity.ok(response);
    }

    // ----------------------------------------------------------------
    // EMAIL MANAGEMENT
    // ----------------------------------------------------------------

    /**
     * Verifies email address and revokes email verification token.
     * 
     * @param request Email verification token value
     * @return ResponseEntity containing account's information
     */
    @PostMapping("/verify-email")
    @Operation(
        summary = "Verify email",
        description = "Verifies the email address of an account"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verify email successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials or account locked")
    })
    public ResponseEntity<AccountInfoDTO> verifyEmail(@Valid @RequestBody EmailVerificationRequestDTO request) {
        // Authenticate an email using the email vefication token
        AccountInfoDTO accountInfo = authService.verifyEmail(request);

        // Return the authentication response with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }

    /**
     * Resends email vefication link and revokes old email verification tokens.
     * 
     * @param request Email to which send the link 
     * @return ResponseEntity containing message of verification link sent
     */
    @PostMapping("/resend-verification")
    @Operation(
        summary = "Resend verification email",
        description = "Resends the verification link to the email address of an account"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verify email successful"),
    })
    public ResponseEntity<GenericMessageResponseDTO> resendEmailVerification(@Valid @RequestBody EmailResendRequestDTO request) {
        // Resend verification link to authenticate an email
        GenericMessageResponseDTO response = authService.resendEmailVerification(request);

        // Return the authentication response with "200 OK" status
        return ResponseEntity.ok(response);
    }

    // ----------------------------------------------------------------
    // PASSWORD MANAGEMENT
    // ----------------------------------------------------------------

    /**
     * Sends password reset link and revokes old password reset tokens.
     *
     * @param request Email to which send the link
     * @return ResponseEntity containing message of password reset link sent
     */
    @PostMapping("/forgot-password")
    @Operation(
        summary = "Send password reset email",
        description = "Sends the password reset link to the email address of an account"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset email sent"),
    })
    public ResponseEntity<GenericMessageResponseDTO> forgotPassword(@Valid @RequestBody PasswordForgotRequestDTO request) {
        // Resend password reset link to authenticate an email
        GenericMessageResponseDTO response = passwordService.forgotPassword(request);

        // Return the authentication response with "200 OK" status
        return ResponseEntity.ok(response);
    }

    /**
     * Sends password reset link and revokes old password reset tokens.
     *
     * @param request Email to which send the link
     * @return ResponseEntity containing message of password reset link sent
     */
    @PostMapping("/reset-password")
    @Operation(
        summary = "Reset password",
        description = "Resets the password of an account"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset email sent"),
        @ApiResponse(responseCode = "401", description = "Invalid password reset token")
    })
    public ResponseEntity<AccountInfoDTO> resetPassword(@Valid @RequestBody PasswordResetRequestDTO request) {
        // Resend password reset link to authenticate an email
        AccountInfoDTO accountInfo = passwordService.resetPassword(request);

        // Return the authentication response with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }
    
}