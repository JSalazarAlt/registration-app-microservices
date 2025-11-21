package com.suyos.userservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.userservice.dto.request.UserUpdateRequestDTO;
import com.suyos.userservice.dto.response.UserProfileDTO;
import com.suyos.userservice.service.UserService;
import com.suyos.common.dto.response.PagedResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

/**
 * REST controller for user profile operations.
 *
 * <p>Handles user profile retrieval and update endpoints.</p>
 *
 * @author Joel Salazar
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(
    name = "User Management",
    description = "Operations for managing user profiles"
)
public class UserController {

    /** Service for user business logic */
    private final UserService userService;

    // ----------------------------------------------------------------
    // ADMIN
    // ----------------------------------------------------------------

    @Operation(
        summary = "List all users (paginated)",
        description = "Retrieves a paginated list of users with sorting options.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Paged list of users retrieved successfully")
        }
    )
    @GetMapping
    public ResponseEntity<PagedResponseDTO<UserProfileDTO>> getAllUsersPaginated(
            @Parameter(description = "Page index (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "desc") String sortDir) {
        // Find all users' profile information paginated
        PagedResponseDTO<UserProfileDTO> users = userService.findAllUsers(page, size, sortBy, sortDir);
        
        // Return users' profile information with "200 OK" status
        return ResponseEntity.ok(users);
    }

    @Operation(
        summary = "Get user profile by ID",
        description = "Retrieves an existing user's profile using their ID.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDTO> getUserById(
            @Parameter(description = "User's unique ID", required = true)
            @PathVariable UUID id) {
        // Find user's profile by ID
        UserProfileDTO userProfile = userService.findUserById(id);
        
        // Return user's profile with "200 OK" status
        return ResponseEntity.ok(userProfile);
    }

    @Operation(
        summary = "Update user profile by ID",
        description = "Updates an existing user's profile using their ID.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User profile updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<UserProfileDTO> updateUserById(
            @Parameter(description = "User's unique ID", required = true)
            @PathVariable UUID id,
            @RequestBody UserUpdateRequestDTO userUpdateDTO) {
        // Update user's profile by ID
        UserProfileDTO userProfile = userService.updateUserById(id, userUpdateDTO);
        
        // Return updated user's profile with "200 OK" status
        return ResponseEntity.ok(userProfile);
    }

    @Operation(
        summary = "Search users by name",
        description = "Performs a case-insensitive search across first and last names.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of matching users retrieved successfully")
        }
    )
    @GetMapping("/search")
    public ResponseEntity<List<UserProfileDTO>> searchUsersByName(
            @Parameter(description = "Partial or full name to search", required = true)
            @RequestParam String name) {
        // Search users by name
        List<UserProfileDTO> users = userService.searchUsersByName(name);
        
        // Return matching users' profile information with "200 OK" status
        return ResponseEntity.ok(users);
    }

    // ----------------------------------------------------------------
    // USER MANAGEMENT
    // ----------------------------------------------------------------

    @Operation(
        summary = "Get current user profile",
        description = "Retrieves the authenticated user's profile.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile(
        @AuthenticationPrincipal Jwt jwt) {
        // Extract account ID from JWT token
        UUID accountId = UUID.fromString(jwt.getSubject());
        
        // Find user's profile by account ID
        UserProfileDTO userProfile = userService.findUserByAccountId(accountId);
        
        // Return user's profile with "200 OK" status
        return ResponseEntity.ok(userProfile);
    }

    @Operation(
        summary = "Update current user profile",
        description = "Updates the authenticated user's profile information.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User profile updated successfully",
                         content = @Content(schema = @Schema(implementation = UserProfileDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    @PutMapping("/me")
    public ResponseEntity<UserProfileDTO> updateCurrentUserProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UserUpdateRequestDTO updateDTO) {
        // Extract account ID from JWT token
        UUID accountId = UUID.fromString(jwt.getSubject());
        
        // Update user's profile by account ID
        UserProfileDTO userProfile = userService.updateUserByAccountId(accountId, updateDTO);
        
        // Return updated user's profile with "200 OK" status
        return ResponseEntity.ok(userProfile);
    }

    // ----------------------------------------------------------------
    // SYNC ENDPOINTS
    // ----------------------------------------------------------------

    @Operation(
        summary = "Sync email update from Auth Service",
        description = "Updates user's email after a change in the Auth service.",
        responses = { @ApiResponse(responseCode = "204", description = "Email updated successfully") }
    )
    @PutMapping("/sync/email/{accountId}")
    public ResponseEntity<Void> syncEmailUpdate(
            @PathVariable UUID accountId,
            @Parameter(description = "New email address", required = true)
            @RequestParam String newEmail) {
        // Update user's email from Auth Service
        userService.mirrorEmailUpdate(accountId, newEmail);
        
        // Return "204 No Content" status
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
        summary = "Sync username update from Auth Service",
        description = "Updates user's username after a change in the Auth service.",
        responses = { @ApiResponse(responseCode = "204", description = "Username updated successfully") }
    )
    @PutMapping("/sync/username/{accountId}")
    public ResponseEntity<Void> syncUsernameUpdate(
            @PathVariable UUID accountId,
            @Parameter(description = "New username", required = true)
            @RequestParam String newUsername) {
        // Update user's username from Auth Service
        userService.mirrorUsernameUpdate(accountId, newUsername);
        
        // Return "204 No Content" status
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}