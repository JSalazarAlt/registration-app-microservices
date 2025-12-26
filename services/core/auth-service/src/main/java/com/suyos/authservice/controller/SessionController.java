package com.suyos.authservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.authservice.dto.response.SessionInfoResponse;
import com.suyos.authservice.service.SessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for session management operations.
 * 
 * <p>Handles session retrieval endpoints.</p>
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(
    name = "Authentication Management", 
    description = "Operations for managing authentication"
)
public class SessionController {

    /** Service for session management */
    private final SessionService sessionService;

    // ----------------------------------------------------------------
    // ADMIN
    // ----------------------------------------------------------------

    /**
     * Retrieves all active sessions by account's ID.
     * 
     * @param accountId Account ID
     * @return List of active sessions
     */
    @Secured("ROLE_ADMIN")
    @Operation(
        summary = "Get all sessions by account ID",
        description = "Retrieves an account's active sessions using its ID",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "User profile updated successfully",
                content = @Content(schema = @Schema(implementation = SessionInfoResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<SessionInfoResponse>> getAllSessionsByAccountId(@PathVariable UUID accountId) {
        return ResponseEntity.ok(sessionService.findAllSessionsByAccountId(accountId));
    }

    // ----------------------------------------------------------------
    // SESSION MANAGEMENT
    // ----------------------------------------------------------------

    /**
     * Retrieves the currently authenticated account's active sessions.
     * 
     * @param accountId Account ID
     * @return List of active sessions of authenticated account with "200 OK" status
     */
    @Operation(
        summary = "Get all authenticated account sessions",
        description = "Retrieves all sessions of the authenticated account",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Sessions retrieved successfully",
                content = @Content(schema = @Schema(implementation = SessionInfoResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/me")
    public ResponseEntity<List<SessionInfoResponse>> getAuthenticatedAccountSessions(@AuthenticationPrincipal Jwt jwt) {
        // Extract authenticated account ID from JWT token
        UUID authenticatedAccountId = UUID.fromString(jwt.getSubject());

        // Find authenticated account's list of sessions
        List<SessionInfoResponse> sessions = sessionService.findAllSessionsByAccountId(authenticatedAccountId);

        // Return authenticated account's list of sessions with "200 OK" status
        return ResponseEntity.ok(sessions);
    }
    
}