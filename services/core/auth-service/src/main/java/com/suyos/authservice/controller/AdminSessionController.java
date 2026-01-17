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

import com.suyos.authservice.dto.response.SessionInfoResponse;
import com.suyos.authservice.model.SessionTerminationReason;
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
@RequestMapping("/api")
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
        description = "Retrieves an account's active sessions using its ID",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Sessions retrieved successfully",
                content = @Content(schema = @Schema(implementation = SessionInfoResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/accounts/{accountId}/sessions")
    public ResponseEntity<List<SessionInfoResponse>> getAllSessionsByAccountId(
        @PathVariable UUID accountId
    ) {
        // Return list of account's active sessions' information with "200
        // OK" status
        return ResponseEntity.ok(sessionService.findAllSessionsByAccountId(accountId));
    }

    /**
     * Retrieves a session by ID.
     * 
     * @param id Session's ID
     * @return Account's list of active sessions' information with "200 OK"
     * status
     */
    @Secured("ROLE_ADMIN")
    @Operation(
        summary = "Get session by ID",
        description = "Retrieves an active session using its ID",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Session retrieved successfully",
                content = @Content(schema = @Schema(implementation = SessionInfoResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/sessions/{id}")
    public ResponseEntity<SessionInfoResponse> getSessionById(
        @PathVariable UUID id
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
        summary = "Terminate sessions by account ID",
        description = "Terminates an account's all active sessions using its ID",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Sessions terminated successfully",
                content = @Content(schema = @Schema(implementation = SessionInfoResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @DeleteMapping("/accounts/{accountId}/sessions")
    public ResponseEntity<Void> terminateAllSessionsByAccountId(
        @PathVariable UUID accountId
    ) {
        // Terminate account's active sessions
        sessionService.terminateAllSessionsByAccountId(accountId, SessionTerminationReason.ADMIN_TERMINATED);

        // Return response with "204 No Content" status
        return ResponseEntity.noContent().build();
    }

    /**
     * Terminates a session by ID.
     * 
     * @param id Session's ID
     * @return Account's list of active sessions' information with "200 OK"
     * status
     */
    @Secured("ROLE_ADMIN")
    @Operation(
        summary = "Terminate session by ID",
        description = "Terminates an active session using its ID",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Session terminated successfully",
                content = @Content(schema = @Schema(implementation = SessionInfoResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<Void> terminateSessionById(
        @PathVariable UUID id
    ) {
        // Terminate session by ID
        sessionService.terminateSessionById(id, SessionTerminationReason.ADMIN_TERMINATED);

        // Return response with "204 No Content" status
        return ResponseEntity.noContent().build();
    }

}