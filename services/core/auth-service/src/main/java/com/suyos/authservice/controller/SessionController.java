package com.suyos.authservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.authservice.dto.response.SessionInfoResponse;
import com.suyos.authservice.model.SessionTerminationReason;
import com.suyos.authservice.service.SessionService;
import com.suyos.common.exception.ApiErrorResponse;

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
    name = "Session Management", 
    description = "Operations for managing sessions"
)
public class SessionController {

    /** Service for session management */
    private final SessionService sessionService;

    // ----------------------------------------------------------------
    // LOOKUP
    // ----------------------------------------------------------------

    /**
     * Retrieves the currently authenticated account's active sessions.
     * 
     * @param jwt Authentication principal containing JWT token
     * @return Authenticated account's list of active sessions' information
     * with "200 OK" status
     */
    @Operation(
        summary = "Get authenticated account's sessions",
        description = "Retrieves all authenticated account's active sessions",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Sessions retrieved successfully",
                content = @Content(schema = @Schema(implementation = SessionInfoResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Invalid or missing JWT token",
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
    @GetMapping("/me")
    public ResponseEntity<List<SessionInfoResponse>> getAuthenticatedAccountSessions(
        @AuthenticationPrincipal Jwt jwt
    ) {
        // Extract authenticated account ID from JWT token
        UUID authenticatedAccountId = UUID.fromString(jwt.getSubject());

        // Find authenticated account's list of active sessions' information
        List<SessionInfoResponse> sessions = sessionService.getAllSessionsByAccountId(authenticatedAccountId);

        // Return authenticated account's list of active sessions' information
        // with "200 OK" status
        return ResponseEntity.ok(sessions);
    }

    // ----------------------------------------------------------------
    // TERMINATION
    // ----------------------------------------------------------------

    /**
     * Terminates the currently authenticated account's active sessions.
     * 
     * @param jwt Authentication principal containing JWT token
     */
    @Operation(
        summary = "Terminate authenticated account's sessions",
        description = "Terminates all the authenticated account's active sessions",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Sessions deleted successfully",
                content = @Content(schema = @Schema(implementation = SessionInfoResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request body or validation error",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Invalid or missing JWT token",
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
    @DeleteMapping("/me")
    public ResponseEntity<Void> terminateAuthenticatedAccountSessions(@AuthenticationPrincipal Jwt jwt) {
        // Extract authenticated account ID from JWT token
        UUID authenticatedAccountId = UUID.fromString(jwt.getSubject());

        // Terminate all authenticated account's sessions
        sessionService.terminateAllSessionsByAccountId(authenticatedAccountId, SessionTerminationReason.USER_TERMINATED);

        // Return response with "204 No Content" status
        return ResponseEntity.noContent().build();
    }

}