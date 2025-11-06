package com.suyos.authservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.authservice.dto.request.AccountUpdateDTO;
import com.suyos.authservice.dto.response.AccountInfoDTO;
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

    /** Service layer for account business logic */
    private final AccountService accountService;

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
        AccountInfoDTO accountInfo = accountService.findAccountByUsername(username);

        // Return account information with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }

    /**
     * Retrieves account information by username.
     * 
     * @param username Username to search for
     * @return ResponseEntity containing account information
     * @throws RuntimeException If account not found
     */
    @GetMapping("/{me}")
    @Operation(
        summary = "Get current logged-in account",
        description = "Retrieves current logged-in account information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account found and returned"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountInfoDTO> getLoggedInAccount(@RequestHeader("Authorization") String authHeader) {
        // Fetch account information by ID
        AccountInfoDTO accountInfo = accountService.findLoggedInAccount(authHeader);

        // Return account information with "200 OK" status
        return ResponseEntity.ok(accountInfo);
    }
    
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
            @Valid @RequestBody AccountUpdateDTO accountUpdateDTO) {

        // Delegate update logic to service
        AccountInfoDTO updatedAccount = accountService.updateLoggedInAccount(authHeader, accountUpdateDTO);

        return ResponseEntity.ok(updatedAccount);
    }

}
