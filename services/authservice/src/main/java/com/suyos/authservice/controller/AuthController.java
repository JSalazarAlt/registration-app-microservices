package com.suyos.authservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.authservice.dto.request.AccountLoginDTO;
import com.suyos.authservice.dto.request.AccountRegistrationDTO;
import com.suyos.authservice.dto.request.EmailResendRequestDTO;
import com.suyos.authservice.dto.request.EmailVerificationRequestDTO;
import com.suyos.authservice.dto.request.RefreshTokenRequestDTO;
import com.suyos.authservice.dto.response.AccountInfoDTO;
import com.suyos.authservice.dto.response.AuthenticationResponseDTO;
import com.suyos.authservice.dto.response.EmailResendResponseDTO;
import com.suyos.authservice.service.AuthService;
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
 * <p>Handles user registration and login endpoints and provides JWT-based
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
    
    /** Service for token management */
    private final TokenService tokenService;

    // REGISTRATION AND LOGIN

    /**
     * Registers a new user account.
     * 
     * @param accountRegistrationDTO Account's registration data
     * @return ResponseEntity containing created account's information
     */
    @PostMapping("/register")
    @Operation(
        summary = "Register new account", 
        description = "Creates a new account with the provided information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Registration successful"),
        @ApiResponse(responseCode = "400", description = "Invalid registration data or email already exists")
    })
    public ResponseEntity<AccountInfoDTO> registerAccount(@Valid @RequestBody AccountRegistrationDTO accountRegistrationDTO) {
        // Create a new account using the registration data
        AccountInfoDTO accountInfoDTO = authService.createAccount(accountRegistrationDTO);
        
        // Return the created account info with "201 Created" status
        return ResponseEntity.status(HttpStatus.CREATED).body(accountInfoDTO);
    }

    /**
     * Authenticates account and returns refresh and access tokens.
     * 
     * @param accountLoginDTO Account's login credentials
     * @return ResponseEntity containing refresh and access tokens
     */
    @PostMapping("/login")
    @Operation(
        summary = "Account login", 
        description = "Authenticates account credentials and returns JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials or account locked")
    })
    public ResponseEntity<AuthenticationResponseDTO> loginAccount(@Valid @RequestBody AccountLoginDTO accountLoginDTO) {
        // Authenticate an account using the login credentials
        AuthenticationResponseDTO authenticationResponseDTO = authService.authenticateAccount(accountLoginDTO);
        
        // Return the authentication response with "200 OK" status
        return ResponseEntity.ok(authenticationResponseDTO);
    }

    /**
     * Deauthenticates account and revokes refresh token.
     * 
     * @param refreshTokenRequestDTO Refresh token request
     * @return ResponseEntity indicating logout success
     */
    @PostMapping("/logout")
    @Operation(
        summary = "Account logout",
        description = "Invalidates account's JWT and refresh tokens"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Logout successful"),
        @ApiResponse(responseCode = "400", description = "Invalid or missing token")
    })
    public ResponseEntity<Void> logoutAccount(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
        // Deauthenticate an account revoking the refresh token
        authService.deauthenticateAccount(refreshTokenRequestDTO);
        
        // Return the logout response with "204 No Content" status
        return ResponseEntity.noContent().build();
    }

    // EMAIL 

    /**
     * Verifies email address and deletes email verification token.
     * 
     * @param emailVerificationTokenRequestDTO Email verification token request
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
    public ResponseEntity<AccountInfoDTO> verifyEmail(@Valid @RequestBody EmailVerificationRequestDTO emailVerificationTokenRequestDTO) {
        // Authenticate an email using the email vefication token
        AccountInfoDTO accountInfo = authService.verifyEmail(emailVerificationTokenRequestDTO);

        // Return the authentication response with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }

    /**
     * Verifies email address and deletes email verification token.
     * 
     * @param emailResendRequestDTO Email verification token request
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
    public ResponseEntity<EmailResendResponseDTO> verifyEmail(@Valid @RequestBody EmailResendRequestDTO emailResendRequestDTO) {
        // Resend verification link to authenticate an email
        EmailResendResponseDTO emailResendResponseDTO = authService.resendEmailVerification(emailResendRequestDTO);

        // Return the authentication response with "200 OK" status
        return ResponseEntity.ok(emailResendResponseDTO);
    }

    // TOKEN MANAGEMENT

    /**
     * Refreshes access token using the refresh token.
     * 
     * @param refreshTokenRequestDTO Refresh token request
     * @return ResponseEntity containing new refresh and access tokens
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh JWT token",
        description = "Generates new JWT token using valid refresh token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<AuthenticationResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
        // Refresh access token using the refresh token
        AuthenticationResponseDTO response = tokenService.refreshToken(refreshTokenRequestDTO);
        
        // Return the authentication response with "200 OK" status
        return ResponseEntity.ok(response);
    }
    
}