package com.suyos.authservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.authservice.dto.AccountInfoDTO;
import com.suyos.authservice.service.AuthService;
import com.suyos.authservice.service.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for account management operations.
 *
 * <p>Handles account retrieval endpoints and provides account information
 * access for the application.</p>
 *
 * @author Joel Salazar
 */
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(
    name = "Account Management", 
    description = "Operations for managing accounts"
)
public class AccountController {

    /** Service layer for authentication business logic */
    private final AuthService authService;
    
    /** Service for managing JWT tokens */
    private final TokenService tokenService;

    /**
     * Retrieves account information by username.
     * 
     * @param username Username to search for
     * @return ResponseEntity containing account information
     * @throws RuntimeException If account not found
     */
    @GetMapping("/{username}")
    @Operation(
        summary = "Get account by username",
        description = "Retrieves account information by username"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account found and returned"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountInfoDTO> getAccountByUsername(
            @Parameter(description = "Account's username", required = true)
            @PathVariable String username) {
        // Fetch account information by username
        AccountInfoDTO accountInfo = authService.findAccountByUsername(username);

        // Return account information with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }
    
}
