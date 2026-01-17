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
import com.suyos.userservice.dto.response.UserProfileResponse;
import com.suyos.userservice.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

/**
 * REST controller for user profile operations.
 *
 * <p>Handles user profile retrieval and update endpoints.</p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(
    name = "User Management",
    description = "Operations for managing user profiles"
)
public class UserController {

    /** Service for user business logic */
    private final UserService userService;

    // ----------------------------------------------------------------
    // LOOKUP
    // ----------------------------------------------------------------

    /**
     * Retrieves the currently authenticated user.
     *
     * @param jwt Authentication principal containing JWT token
     * @return Authenticated user's profile with "200 OK" status
     */
    @Operation(
        summary = "Get authenticated user",
        description = "Retrieves the authenticated user's profile.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully", 
                         content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
        }
    )
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getAuthenticatedUser(
        @AuthenticationPrincipal Jwt jwt
    ) {
        // Extract authenticated account ID from JWT token
        UUID authenticatedAccountId = UUID.fromString(jwt.getSubject());
        
        // Find authenticated user's profile
        UserProfileResponse userProfile = userService.findUserByAccountId(authenticatedAccountId);
        
        // Return user's profile with "200 OK" status
        return ResponseEntity.ok(userProfile);
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    /**
     * Updates the currently authenticated user.
     *
     * @param jwt Authentication principal containing JWT token
     * @param updateDTO DTO containing updated profile fields
     * @return Updated authenticated user's profile with "200 OK" status
     */
    @Operation(
        summary = "Update authenticated user",
        description = "Updates the authenticated user's profile",
        responses = {
            @ApiResponse(responseCode = "200", description = "User profile updated successfully",
                         content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
        }
    )
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateAuthenticatedUser(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody UserUpdateRequest updateDTO
    ) {
        // Extract authenticated account ID from JWT token
        UUID authenticatedAccountId = UUID.fromString(jwt.getSubject());
        
        // Update authenticated user's profile
        UserProfileResponse userProfile = userService.updateUserByAccountId(authenticatedAccountId, updateDTO);
        
        // Return updated user's profile with "200 OK" status
        return ResponseEntity.ok(userProfile);
    }

}