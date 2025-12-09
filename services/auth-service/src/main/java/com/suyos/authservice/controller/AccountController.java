package com.suyos.authservice.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
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
import com.suyos.authservice.service.AccountService;
import com.suyos.common.dto.response.PagedResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for account management operations.
 *
 * <p>Handles account retrieval and update endpoints.</p>
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
     * Retrieves a paginated list of all accounts.
     * 
     * @param page Zero-based page index
     * @param size Page size
     * @param sortBy Field to sort by
     * @param sortDir Sort direction (asc/desc)
     * @return Paginated list of accounts' information with "200 OK" status
     */
    @Secured("ROLE_ADMIN")
    @GetMapping
    @Operation(
        summary = "Get all accounts", 
        description = "Retrieves a paginated list of accounts with sorting options",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Accounts retrieved successfully",
                content = @Content(schema = @Schema(implementation = PagedResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or sort parameters"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<PagedResponseDTO<AccountInfoDTO>> getAllAccounts(
            @Parameter(description = "Zero-based page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of records per page (max 100)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "email") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        // Find paginated list of accounts' information
        PagedResponseDTO<AccountInfoDTO> accountInfos = accountService.findAllAccounts(page, 
            size, sortBy, sortDir);
        
        // Return paginated list of accounts' information with "200 OK" status
        return ResponseEntity.ok(accountInfos);
    }

    /**
     * Retrieves an account's information by ID.
     * 
     * @param id Account's ID to search for
     * @return Account's information with "200 OK" status
     */
    @Secured("ROLE_ADMIN")
    @GetMapping("/{id}")
    @Operation(
        summary = "Get account by ID",
        description = "Retrieves account's information using its ID",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Account retrieved successfully",
                content = @Content(schema = @Schema(implementation = AccountInfoDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<AccountInfoDTO> getAccountById(
            @Parameter(description = "Account's ID", required = true)
            @PathVariable UUID id) {
        // Find account's information by id
        AccountInfoDTO accountInfo = accountService.findAccountById(id);

        // Return account's information with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }

    /**
     * Retrieves an account's information by username.
     * 
     * @param username Username to search for
     * @return Account's information with "200 OK" status
     */
    @Secured("ROLE_ADMIN")
    @GetMapping("/username/{username}")
    @Operation(
        summary = "Get account by username",
        description = "Retrieves account's information using its username",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Account retrieved successfully",
                content = @Content(schema = @Schema(implementation = AccountInfoDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<AccountInfoDTO> getAccountByUsername(
            @Parameter(description = "Account's username", required = true)
            @PathVariable String username) {
        // Find account's information by username
        AccountInfoDTO accountInfo = accountService.findAccountByUsername(username);

        // Return account's information with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }

    // ----------------------------------------------------------------
    // ACCOUNT MANAGEMENT
    // ----------------------------------------------------------------

    /**
     * Retrieves the currently authenticated account's information.
     * 
     * @param jwt Authentication principal containing JWT token
     * @return Authenticated account's information with "200 OK" status
     */
    @GetMapping("/me")
    @Operation(
        summary = "Get authenticated account",
        description = "Retrieves the authenticated account's information",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Account retrieved successfully",
                content = @Content(schema = @Schema(implementation = AccountInfoDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<AccountInfoDTO> getAuthenticatedAccount(@AuthenticationPrincipal Jwt jwt) {
        // Extract authenticated account's ID from access token
        UUID authenticatedAccountId = UUID.fromString(jwt.getSubject());
        
        // Find authenticated account
        AccountInfoDTO accountInfo = accountService.findAccountById(authenticatedAccountId);

        // Return authenticated account's information with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }
    
    /**
     * Updates the currently authenticated account's information.
     * 
     * @param jwt Authentication principal containing JWT token
     * @param request Account's update data
     * @return Updated authenticated account's information with "200 OK" status
     */
    @PatchMapping("/me")
    @Operation(
        summary = "Update authenticated account",
        description = "Updates the authenticated account's information",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Account updated successfully",
                content = @Content(schema = @Schema(implementation = AccountInfoDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation errors"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<AccountInfoDTO> updateAuthenticatedAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AccountUpdateRequestDTO request) {
        // Extract authenticated account's ID from access token
        UUID authenticatedAccountId = UUID.fromString(jwt.getSubject());

        // Update authenticated account
        AccountInfoDTO accountInfo = accountService.updateAccountById(authenticatedAccountId, request);
        
        // Return updated authenticated account's information with "200 OK"
        // status
        return ResponseEntity.ok(accountInfo);
    }

    /**
     * Soft-deletes the currently authenticated account's information.
     * 
     * @param jwt Authentication principal containing JWT token
     * @return Soft-deleted authenticated account's information with "200 OK"
     * status
     */
    @DeleteMapping("/me")
    @Operation(
        summary = "Soft-delete authenticated account",
        description = "Soft-deletes the authenticated account's information",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Account soft deleted successfully",
                content = @Content(schema = @Schema(implementation = AccountInfoDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<AccountInfoDTO> deleteAuthenticatedAccount(@AuthenticationPrincipal Jwt jwt) {
        // Extract authenticated account's ID from access token
        UUID authenticatedAccountId = UUID.fromString(jwt.getSubject());

        // Soft-delete authenticated account
        AccountInfoDTO accountInfo = accountService.softDeleteAccountById(authenticatedAccountId);
        
        // Return soft-deleted authenticated account's information with "200 OK"
        // status
        return ResponseEntity.ok(accountInfo);
    }

}