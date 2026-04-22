package com.suyos.authservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.authservice.dto.response.SessionResponse;
import com.suyos.authservice.model.SessionTerminationReason;
import com.suyos.authservice.service.SessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
    name = "Admin Session Management", 
    description = "Admin operations for managing sessions"
)
public class AdminSessionController {

    /** Service for session management */
    private final SessionService sessionService;

    // ----------------------------------------------------------------
    // LOOKUP
    // ----------------------------------------------------------------

    /**
     * Retrieves an account's active sessions by account ID.
     * 
     * @param accountId Account ID
     * @return Account's list of active sessions' information with "200 OK"
     * status
     */
    @Secured("ROLE_ADMIN")
    @Operation(
        summary = "Get all sessions by account ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
        }
    )
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<SessionResponse>> getAllSessionsByAccountId(
        @Parameter(description = "Account ID") @PathVariable UUID accountId
    ) {
        // Return list of account's active sessions' information with "200 OK" status
        return ResponseEntity.ok(sessionService.getAllSessionsByAccountId(accountId));
    }

    /**
     * Retrieves a session by ID.
     * 
     * @param id Session ID
     * @return Account's list of active sessions' information with "200 OK"
     * status
     */
    @Secured("ROLE_ADMIN")
    @Operation(
        summary = "Get session by ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Session retrieved successfully"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Session not found", content = @Content),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> getSessionById(
        @Parameter(description = "Session ID") @PathVariable UUID id
    ) {
        // Return session's information with "200 OK" status
        return ResponseEntity.ok(sessionService.findSessionById(id));
    }

    // ----------------------------------------------------------------
    // TERMINATION
    // ----------------------------------------------------------------

    /**
     * Terminates an account's active sessions using its account ID.
     * 
     * @param accountId Account ID
     * @return Response with "204 No Content" status
     */
    @Secured("ROLE_ADMIN")
    @Operation(
        summary = "Terminate all sessions by account ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Sessions terminated successfully"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
        }
    )
    @DeleteMapping("/account/{accountId}")
    public ResponseEntity<Void> terminateAllSessionsByAccountId(
        @Parameter(description = "Account ID") @PathVariable UUID accountId
    ) {
        // Terminate account's active sessions
        sessionService.terminateAllSessionsByAccountId(accountId, SessionTerminationReason.ADMIN_TERMINATED);

        // Return response with "204 No Content" status
        return ResponseEntity.noContent().build();
    }

    /**
     * Terminates a session by ID.
     * 
     * @param id Session ID
     * @return Account's list of active sessions' information with "200 OK"
     * status
     */
    @Secured("ROLE_ADMIN")
    @Operation(
        summary = "Terminate session by ID",
        description = "Terminates an active session using its ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Session terminated successfully"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Session not found", content = @Content),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> terminateSessionById(
        @Parameter(description = "Session ID") @PathVariable UUID id
    ) {
        // Terminate session by ID
        sessionService.terminateSessionById(id, SessionTerminationReason.ADMIN_TERMINATED);

        // Return response with "204 No Content" status
        return ResponseEntity.noContent().build();
    }

}