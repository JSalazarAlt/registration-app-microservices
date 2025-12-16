package com.suyos.sessionservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.sessionservice.dto.SessionInfoDTO;
import com.suyos.sessionservice.service.SessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for session management operations.
 * 
 * @author Joel Salazar
 */
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Tag(
    name = "Session Management",
    description = "Operations for managing sessions"
)
public class SessionController {

    /** Service for sessions business logic */
    private final SessionService sessionService;

    // ----------------------------------------------------------------
    // ADMIN
    // ----------------------------------------------------------------

    /**
     * Retrieves all active sessions for an account.
     * 
     * @param accountId Account ID
     * @return List of active sessions
     */
    @Secured("ROLE_ADMIN")
    @Operation(
        summary = "Get all sessions for an account",
        description = "Updates a user's profile using their ID.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "User profile updated successfully",
                content = @Content(schema = @Schema(implementation = SessionInfoDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<SessionInfoDTO>> getAllSessionsByAccountId(@PathVariable UUID accountId) {
        return ResponseEntity.ok(sessionService.findAllSessionsByAccount(accountId));
    }

    /**
     * Terminates a specific session.
     * 
     * @param sessionId Session ID
     * @return No content response
     */
    /*/
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> terminateSession(@PathVariable UUID id, SessionTerminationReason reason) {
        sessionService.terminateSession(id, reason);
        return ResponseEntity.noContent().build();
    }
    */

    /**
     * Terminates all sessions for an account.
     * 
     * @param accountId Account ID
     * @return No content response
     */
    @DeleteMapping("/account/{accountId}")
    public ResponseEntity<Void> terminateAllSessionsByAccountId(@PathVariable UUID accountId) {
        sessionService.terminateAllSessionsByAccountId(accountId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves all active sessions for an account.
     * 
     * @param accountId Account ID
     * @return List of active sessions
     */
    @Secured("ROLE_ADMIN")
    @Operation(
        summary = "Get all sessions for an account",
        description = "Updates a user's profile using their ID.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "User profile updated successfully",
                content = @Content(schema = @Schema(implementation = SessionInfoDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/me")
    public ResponseEntity<List<SessionInfoDTO>> getAuthenticatedAccountSessions(@AuthenticationPrincipal Jwt jwt) {
        // Extract authenticated account ID from JWT token
        UUID authenticatedAccountId = UUID.fromString(jwt.getSubject());

        // Find authenticated account's list of sessions
        List<SessionInfoDTO> sessions = sessionService.findAllSessionsByAccount(authenticatedAccountId);

        // Return authenticated account's list of sessions with "200 OK" status
        return ResponseEntity.ok(sessions);
    }
    
}