package com.suyos.authservice.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.authservice.dto.request.AccountUpdateRequest;
import com.suyos.authservice.dto.response.AccountInfoResponse;
import com.suyos.authservice.service.AccountService;

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
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(
    name = "Account Management", 
    description = "Operations for managing accounts"
)
public class AccountController {

    /** Service for account business logic */
    private final AccountService accountService;

    // ----------------------------------------------------------------
    // LOOKUP
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
                content = @Content(schema = @Schema(implementation = AccountInfoResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<AccountInfoResponse> getAuthenticatedAccount(
        @AuthenticationPrincipal Jwt jwt
    ) {
        // Extract authenticated account's ID from access token
        UUID authenticatedAccountId = UUID.fromString(jwt.getSubject());
        
        // Find authenticated account
        AccountInfoResponse accountInfo = accountService.findAccountById(authenticatedAccountId);

        // Return authenticated account's information with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------
    
    /**
     * Updates the currently authenticated account.
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
                content = @Content(schema = @Schema(implementation = AccountInfoResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation errors"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<AccountInfoResponse> updateAuthenticatedAccount(
        @AuthenticationPrincipal Jwt jwt,
        @Parameter(description = "Account's update data") @Valid @RequestBody AccountUpdateRequest request
    ) {
        // Extract authenticated account's ID from access token
        UUID authenticatedAccountId = UUID.fromString(jwt.getSubject());

        // Update authenticated account
        AccountInfoResponse accountInfo = accountService.updateAccountById(authenticatedAccountId, request);
        
        // Return updated authenticated account's information with "200 OK"
        // status
        return ResponseEntity.ok(accountInfo);
    }

    // ----------------------------------------------------------------
    // SOFT-DELETION
    // ----------------------------------------------------------------

    /**
     * Soft-deletes the currently authenticated account.
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
                content = @Content(schema = @Schema(implementation = AccountInfoResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<AccountInfoResponse> softDeleteAuthenticatedAccount(
        @AuthenticationPrincipal Jwt jwt
    ) {
        // Extract authenticated account's ID from access token
        UUID authenticatedAccountId = UUID.fromString(jwt.getSubject());

        // Soft-delete authenticated account
        AccountInfoResponse accountInfo = accountService.softDeleteAccountById(authenticatedAccountId);
        
        // Return soft-deleted authenticated account's information with "200
        // OK" status
        return ResponseEntity.ok(accountInfo);
    }

}