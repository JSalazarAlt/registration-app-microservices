package com.suyos.authservice.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.authservice.dto.request.AccountUpdateRequest;
import com.suyos.authservice.dto.response.AccountInfoResponse;
import com.suyos.authservice.service.AccountService;
import com.suyos.common.dto.response.PagedResponseDTO;
import com.suyos.common.exception.ApiErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for account management admin operations.
 *
 * <p>Handles account retrieval and update endpoints.</p>
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(
    name = "Admin Account Management", 
    description = "Admin operations for managing accounts"
)
public class AdminAccountController {

    /** Service for account business logic */
    private final AccountService accountService;

    // ----------------------------------------------------------------
    // LOOKUP
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
            @ApiResponse(
                responseCode = "400",
                description = "Invalid pagination or sort parameters",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Access denied",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
        }
    )
    public ResponseEntity<PagedResponseDTO<AccountInfoResponse>> getAllAccounts(
        @Parameter(description = "Zero-based page number") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Number of records per page (max 100)") @RequestParam(defaultValue = "10") int size,
        @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "email") String sortBy,
        @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir,
        @Parameter(description = "Text to search for") @RequestParam(required = false) String searchText
    ) {
        // Retrieve a paginated response of accounts' information
        PagedResponseDTO<AccountInfoResponse> accountInfos = accountService.getAllAccounts(
            page,
            size,
            sortBy,
            sortDir,
            searchText
        );
        
        // Return paginated response of accounts' information with "200 OK" status
        return ResponseEntity.ok(accountInfos);
    }

    /**
     * Retrieves an account's information by ID.
     * 
     * @param id Account ID to search for
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
                content = @Content(schema = @Schema(implementation = AccountInfoResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Access denied",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Account not found",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
        }
    )
    public ResponseEntity<AccountInfoResponse> getAccountById(
        @Parameter(description = "Account ID") @PathVariable UUID id
    ) {
        // Find account's information by ID
        AccountInfoResponse accountInfo = accountService.getAccountById(id);

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
                content = @Content(schema = @Schema(implementation = AccountInfoResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Access denied",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Account not found",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
        }
    )
    public ResponseEntity<AccountInfoResponse> getAccountByUsername(
        @Parameter(description = "Account's username") @PathVariable String username
    ) {
        // Find account's information by username
        AccountInfoResponse accountInfo = accountService.getAccountByUsername(username);

        // Return account's information with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------
    
    /**
     * Updates an account by ID.
     * 
     * @param id Account ID to update
     * @param request Account's update data
     * @return Updated authenticated account's information with "200 OK" status
     */
    @PatchMapping("/{id}")
    @Operation(
        summary = "Update account by ID",
        description = "Updates an account's information using its ID",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Account updated successfully",
                content = @Content(schema = @Schema(implementation = AccountInfoResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request body or validation errors",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Access denied",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Account not found",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
        }
    )
    public ResponseEntity<AccountInfoResponse> updateAccountById(
        @Parameter(description = "Account ID") @PathVariable UUID id,
        @Parameter(description = "Account's update data") @Valid @RequestBody AccountUpdateRequest request
    ) {
        // Update account's information by ID
        AccountInfoResponse updatedAccountInfo = accountService.updateAccountById(id, request);
        
        // Return updated account's information with "200 OK" status
        return ResponseEntity.ok(updatedAccountInfo);
    }

    // ----------------------------------------------------------------
    // SOFT DELETION
    // ----------------------------------------------------------------

    /**
     * Soft deletes an account by ID.
     * 
     * @param id Account ID to soft delete
     * @return Soft-deleted authenticated account's information with "200 OK"
     * status
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Soft delete account by ID",
        description = "Soft deletes an account's information using its ID",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Account soft deleted successfully",
                content = @Content(schema = @Schema(implementation = AccountInfoResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Access denied",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Account not found",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
        }
    )
    public ResponseEntity<AccountInfoResponse> softDeleteAccountById(
        @Parameter(description = "Account ID") @PathVariable UUID id
    ) {
        // Soft delete account's information by ID
        AccountInfoResponse softDeleteAccountInfo = accountService.softDeleteAccountById(id);

        // Return soft-deleted account's information with "200 OK" status
        return ResponseEntity.ok(softDeleteAccountInfo);
    }

}