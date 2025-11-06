package com.suyos.authservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.authservice.dto.request.AccountLoginDTO;
import com.suyos.authservice.dto.request.AccountRegistrationDTO;
import com.suyos.authservice.dto.request.TokenRequestDTO;
import com.suyos.authservice.dto.response.AccountInfoDTO;
import com.suyos.authservice.dto.response.AuthenticationResponseDTO;
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
    
    /** Service layer for authentication business logic */
    private final AuthService authService;
    
    /** Service for managing JWT tokens */
    private final TokenService tokenService;

    /**
     * Registers a new user account.
     * 
     * @param accountRegistrationDTO Account's registration data
     * @return ResponseEntity containing the created account info or error message
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
     * Authenticates an account during a login attempt and returns JWT token.
     * 
     * @param accountLoginDTO Account's login credentials
     * @return ResponseEntity containing JWT token and account ID or error message
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
        AuthenticationResponseDTO authResponse = authService.authenticateAccount(accountLoginDTO);
        // Return the authentication response with "200 OK" status
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Deauthenticates an account during a logout attempt.
     * 
     * @param tokenRequestDTO Logout token request
     * @return ResponseEntity indicating logout success or error message
     */
    @PostMapping("/logout")
    @Operation(summary = "Account logout", description = "Invalidates the account's JWT token and refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Logout successful"),
        @ApiResponse(responseCode = "400", description = "Invalid or missing token")
    })
    public ResponseEntity<Void> logoutAccount(@RequestBody TokenRequestDTO tokenRequestDTO) {
        // Deauthenticate an account revoking the refresh token
        authService.deauthenticateAccount(tokenRequestDTO);
        // Return the logout response with "204 No Content" status
        return ResponseEntity.noContent().build();
    }

    /**
     * Refreshes JWT token using refresh token.
     * 
     * @param tokenRequestDTO Refresh token request
     * @return ResponseEntity containing new JWT token or error message
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
    public ResponseEntity<AuthenticationResponseDTO> refreshToken(@RequestBody TokenRequestDTO tokenRequestDTO) {
        // Refresh the JWT token using the refresh token
        AuthenticationResponseDTO response = tokenService.refreshToken(tokenRequestDTO.getRefreshToken());
        // Return the authentication response with "200 OK" status
        return ResponseEntity.ok(response);
    }
    
}