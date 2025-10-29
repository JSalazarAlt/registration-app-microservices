package com.suyos.authservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.authservice.dto.AccountInfoDTO;
import com.suyos.authservice.dto.AccountLoginDTO;
import com.suyos.authservice.dto.AccountUpsertDTO;
import com.suyos.authservice.dto.AuthenticationResponseDTO;
import com.suyos.authservice.dto.RefreshTokenRequestDTO;
import com.suyos.authservice.service.AuthService;
import com.suyos.authservice.service.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
    name = "Authentication and Account Management", 
    description = "Operations for managing authentication and accounts"
)
public class AuthController {
    
    /** Service layer for authentication business logic */
    private final AuthService authService;
    
    /** Service for managing JWT tokens */
    private final TokenService tokenService;

    /**
     * Registers a new user account.
     * 
     * @param accountRegistrationDTO Account registration data
     * @return ResponseEntity containing the created account's info or error message
     */
    @PostMapping("/register")
    @Operation(
        summary = "Register new account", 
        description = "Creates a new user account with the provided information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Account registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid registration data or email already exists")
    })
    public ResponseEntity<AccountInfoDTO> registerAccount(@Valid @RequestBody AccountUpsertDTO accountRegistrationDTO,
                                                      HttpServletRequest request) {
        AccountInfoDTO accountInfoDTO = authService.createAccount(accountRegistrationDTO, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountInfoDTO);
    }

    /**
     * Authenticates a login attempt and returns JWT token.
     * 
     * @param accountLoginDTO Account login credentials
     * @return ResponseEntity containing JWT token and user profile or error message
     */
    @PostMapping("/login")
    @Operation(summary = "Account login", description = "Authenticates account credentials and returns JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials or account locked")
    })
    public ResponseEntity<AuthenticationResponseDTO> loginUser(@Valid @RequestBody AccountLoginDTO accountLoginDTO,
                                                              HttpServletRequest request) {
        AuthenticationResponseDTO authResponse = authService.authenticateAccount(accountLoginDTO, request);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Logs out a user by blacklisting their JWT token.
     * 
     * @param request the HTTP request containing the Authorization header
     * @return ResponseEntity indicating logout success
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidates the user's JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "400", description = "Invalid or missing token")
    })
    public ResponseEntity<String> logoutUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok("Logout successful");
        }
        return ResponseEntity.badRequest().body("No valid token found");
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponseDTO> refresh(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
        AuthenticationResponseDTO response = tokenService.refreshToken(refreshTokenRequestDTO.getRefreshToken());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Example "me" endpoint to return account info for current authenticated user.
     * GET /api/v1/auth/me
     *
     * Implementation note: Your JwtService exposes methods to validate/extract tokens,
     * while SecurityConfig likely wires the Authentication. If you prefer SecurityContextHolder,
     * replace token extraction with principal lookup. This example shows header-based extraction.
     */
    /*
    @GetMapping("/me")
    public ResponseEntity<AccountInfoDTO> me(@RequestHeader HttpHeaders headers) {
         
        String token = jwtService == null ? null : jwtService.extractTokenFromHeaders(headers);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = jwtService.extractUsername(token);
        // if your AuthService has getAccountByEmail/use username method, call it; fallback: getAccountById if available.
        // Assuming AuthService has a method to get account by username/email (if not, adapt accordingly)
        AccountInfoDTO accountInfo = authService.getAccountByEmail(username);
        return ResponseEntity.ok(accountInfo);
        
    }
    */
}