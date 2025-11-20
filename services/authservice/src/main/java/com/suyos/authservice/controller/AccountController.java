package com.suyos.authservice.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.authservice.dto.request.AccountUpdateRequestDTO;
import com.suyos.authservice.dto.response.AccountInfoDTO;
import com.suyos.authservice.dto.response.PagedResponseDTO;
import com.suyos.authservice.service.AccountService;

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

    // ----------------------------------------------------------------
    // ADMIN
    // ----------------------------------------------------------------

    /**
     * Retrieves all accounts' information.
     * 
     * @return All accounts' information
     */
    @GetMapping
    @Operation(
        summary = "Get accounts paginated", 
        description = "Retrieves accounts with pagination and sorting"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success - Accounts found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Access denied")
    })
    public ResponseEntity<PagedResponseDTO<AccountInfoDTO>> getAllAccounts(
            @Parameter(description = "Zero-based page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of records per page (max 100)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "email") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        // Find all accounts' information
        PagedResponseDTO<AccountInfoDTO> accountInfos = accountService.findAllAccounts(page, 
            size, sortBy, sortDir);
        
        // Return accounts' information with "200 OK" status
        return ResponseEntity.ok(accountInfos);
    }

    /**
     * Retrieves account information by ID.
     * 
     * @param id Account's ID to search for
     * @return Account's information
     * @throws RuntimeException If account not found
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get account by ID",
        description = "Retrieves account information by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success - Account found and returned"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Access denied"),
        @ApiResponse(responseCode = "404", description = "Not Found - Account not found")
    })
    public ResponseEntity<AccountInfoDTO> getAccountById(
            @Parameter(description = "Account's ID", required = true)
            @PathVariable UUID id) {
        // Find account's information by id
        AccountInfoDTO accountInfo = accountService.findAccountById(id);

        // Return account information with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }

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
        @ApiResponse(responseCode = "200", description = "Success - Account found and returned"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Access denied"),
        @ApiResponse(responseCode = "404", description = "Not Found - Account not found")
    })
    public ResponseEntity<AccountInfoDTO> getAccountByUsername(
            @Parameter(description = "Account's username", required = true)
            @PathVariable String username) {
        // Find account's information by username
        AccountInfoDTO accountInfo = accountService.findAccountByUsername(username);

        // Return account information with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }

    // ----------------------------------------------------------------
    // ACCOUNT MANAGEMENT
    // ----------------------------------------------------------------

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
        @ApiResponse(responseCode = "200", description = "Success - Account found and returned"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "Not Found - Account not found")
    })
    public ResponseEntity<AccountInfoDTO> getLoggedInAccount(@AuthenticationPrincipal Jwt jwt) {
        // Extract logged-in account's ID from access token
        UUID loggedInAccountId = UUID.fromString(jwt.getSubject());
        
        // Find logged-in account
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
        @ApiResponse(responseCode = "200", description = "Success - Account found and updated"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "Not Found - Account not found")
    })
    public ResponseEntity<AccountInfoDTO> updateLoggedInAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AccountUpdateRequestDTO request) {
        // Extract logged-in account's ID from access token
        UUID loggedInAccountId = UUID.fromString(jwt.getSubject());

        // Update logged-in account
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
    @DeleteMapping("/me")
    @Operation(
        summary = "Soft delete current logged-in account",
        description = "Soft deletes the currently logged-in account"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success - Account found and deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "Not Found - Account not found")
    })
    public ResponseEntity<AccountInfoDTO> deleteLoggedInAccount(@AuthenticationPrincipal Jwt jwt) {
        // Extract logged-in account ID from access token
        UUID loggedInAccountId = UUID.fromString(jwt.getSubject());

        // Soft delete logged-in account
        AccountInfoDTO accountInfo = accountService.softDeleteAccountById(loggedInAccountId);
        
        // Return soft deleted logged-in account's information with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }

}