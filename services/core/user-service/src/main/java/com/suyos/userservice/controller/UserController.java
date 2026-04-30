package com.suyos.userservice.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.userservice.dto.request.UserUpdateRequest;
import com.suyos.userservice.dto.response.UserResponse;
import com.suyos.userservice.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(
    name = "User Management",
    description = "Operations for managing user profiles"
)
public class UserController {

    private final UserService userService;

    // ----------------------------------------------------------------
    // RETRIEVAL
    // ----------------------------------------------------------------

    /**
     * Gets the currently authenticated user.
     *
     * @param jwt Authentication principal containing JWT token
     * @return Authenticated user response with "200 OK" status
     */
    @Operation(
        description = "Gets the authenticated user.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
        }
    )
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getAuthenticatedUser(
        @AuthenticationPrincipal Jwt jwt
    ) {
        // Extract authenticated account ID from JWT token
        UUID authenticatedAccountId = UUID.fromString(jwt.getSubject());
        
        // Get authenticated user
        UserResponse userResponse = userService.getUserByAccountId(authenticatedAccountId);
        
        // Return user response with "200 OK" status
        return ResponseEntity.ok(userResponse);
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    /**
     * Updates the currently authenticated user.
     *
     * @param jwt Authentication principal containing JWT token
     * @param request Data used to update the user
     * @return Updated authenticated user response with "200 OK" status
     */
    @Operation(
        description = "Updates the authenticated user.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
        }
    )
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateAuthenticatedUser(
        @AuthenticationPrincipal Jwt jwt,
        @Parameter(description = "Data to update the existing user") @RequestBody UserUpdateRequest request
    ) {
        // Extract authenticated account ID from JWT token
        UUID authenticatedAccountId = UUID.fromString(jwt.getSubject());
        
        // Update authenticated user
        UserResponse userResponse = userService.updateUserByAccountId(authenticatedAccountId, request);
        
        // Return updated user response with "200 OK" status
        return ResponseEntity.ok(userResponse);
    }

}