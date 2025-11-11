package com.suyos.authservice.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.authservice.dto.request.AccountUpdateRequestDTO;
import com.suyos.authservice.dto.response.AccountInfoDTO;
import com.suyos.authservice.service.AccountService;
import com.suyos.authservice.service.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for account management operations.
 *
 * <p>Handles account retrieval and update endpoints.</p>
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

    /** Service for account business logic */
    private final AccountService accountService;

    /** Service for token management */
    private final TokenService tokenService;

    /**
     * Retrieves account information by username.
     * 
     * @param username Username to search for
     * @return Account's information
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
        AccountInfoDTO accountInfo = accountService.findAccountByUsername(username);

        // Return account information with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }

    /**
     * Retrieves current logged-in account.
     * 
     * @param authHeader Authorization header with Bearer token
     * @return Logged-in account information
     * @throws RuntimeException If account not found
     */
    @GetMapping("/me")
    @Operation(
        summary = "Get current logged-in account",
        description = "Retrieves current logged-in account information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account found and returned"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountInfoDTO> getLoggedInAccount(@RequestHeader("Authorization") String authHeader) {
        // Extract logged-in account's ID from access token
        UUID loggedInAccountId = tokenService.extractAccountIdFromAccessToken(authHeader);
        
        // Find the logged-in account
        AccountInfoDTO accountInfo = accountService.findAccountById(loggedInAccountId);

        // Return logged-in account's information with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }
    
    /**
     * Updates current logged-in account.
     * 
     * @param authHeader Authorization header with Bearer token
     * @return Updated logged-in account's information
     * @throws RuntimeException If account not found
     */
    @PatchMapping("/me")
    @Operation(
        summary = "Update current logged-in account",
        description = "Updates fields of the currently logged-in account"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountInfoDTO> updateLoggedInAccount(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AccountUpdateRequestDTO request) {
        // Extract logged-in account ID from access token
        UUID loggedInAccountId = tokenService.extractAccountIdFromAccessToken(authHeader);

        // Update the logged-in account
        AccountInfoDTO accountInfo = accountService.updateAccountById(loggedInAccountId, request);
        
        // Return updated logged-in account's information with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }

    /**
     * Soft deletes current logged-in account.
     * 
     * @param authHeader Authorization header with Bearer token
     * @return Soft deleted logged-in account's information
     * @throws RuntimeException If account not found
     */
    @PatchMapping("/me")
    @Operation(
        summary = "Soft delete current logged-in account",
        description = "Soft deletes the currently logged-in account"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountInfoDTO> deleteLoggedInAccount(@RequestHeader("Authorization") String authHeader) {
        // Extract logged-in account ID from access token
        UUID loggedInAccountId = tokenService.extractAccountIdFromAccessToken(authHeader);

        // Soft delete the logged-in account
        AccountInfoDTO accountInfo = accountService.deleteAccountById(loggedInAccountId);
        
        // Return soft deleted logged-in account's information with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }

}