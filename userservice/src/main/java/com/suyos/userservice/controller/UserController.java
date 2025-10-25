package com.suyos.userservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.userservice.dto.PagedResponseDTO;
import com.suyos.userservice.dto.UserProfileDTO;
import com.suyos.userservice.dto.UserUpdateDTO;
import com.suyos.userservice.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing user profile operations.
 *
 * <p>Exposes endpoints for both end-users (via /me routes) and administrative 
 * or internal use (via /{userId} routes).</p>
 *
 * <p>Authenticated users are identified by their accountId from the JWT token
 * (simulated here via request parameter for demonstration).</p>
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

    /** Service for handling user profile logic */
    private final UserService userService;

    // ----------------------------------------------------------------
    // ADMIN / INTERNAL ENDPOINTS
    // ----------------------------------------------------------------

    @Operation(
        summary = "Get user profile by ID (Admin)",
        description = "Retrieves an existing user's profile using their ID.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully",
                         content = @Content(schema = @Schema(implementation = UserProfileDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDTO> getUserById(
            @Parameter(description = "User's unique ID", required = true)
            @PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserProfileById(userId));
    }

    @Operation(
        summary = "Update user profile by ID (Admin)",
        description = "Updates an existing user's profile using their ID.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User profile updated successfully",
                         content = @Content(schema = @Schema(implementation = UserProfileDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    @PutMapping("/{userId}")
    public ResponseEntity<UserProfileDTO> updateUserById(
            @Parameter(description = "User's unique ID", required = true)
            @PathVariable UUID userId,
            @RequestBody UserUpdateDTO userUpdateDTO) {
        return ResponseEntity.ok(userService.updateUserProfileById(userId, userUpdateDTO));
    }

    @Operation(
        summary = "Check if user exists by ID",
        description = "Checks whether a user exists for a given ID.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Returns true if user exists, false otherwise")
        }
    )
    @GetMapping("/{userId}/exists")
    public ResponseEntity<Boolean> existsById(
            @Parameter(description = "User's unique ID", required = true)
            @PathVariable UUID userId) {
        return ResponseEntity.ok(userService.existsById(userId));
    }

    // ----------------------------------------------------------------
    // USER-FACING ENDPOINTS
    // ----------------------------------------------------------------

    @Operation(
        summary = "Get profile by account ID",
        description = "Retrieves a user's profile using their accountId.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully",
                         content = @Content(schema = @Schema(implementation = UserProfileDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    @GetMapping("/account/{accountId}")
    public ResponseEntity<UserProfileDTO> getProfileByAccountId(
            @Parameter(description = "Account ID associated with the user", required = true)
            @PathVariable UUID accountId) {
        return ResponseEntity.ok(userService.getUserProfileByAccountId(accountId));
    }

    @Operation(
        summary = "Update profile by account ID",
        description = "Updates the profile information for the given accountId.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User profile updated successfully",
                         content = @Content(schema = @Schema(implementation = UserProfileDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    @PutMapping("/account/{accountId}")
    public ResponseEntity<UserProfileDTO> updateProfileByAccountId(
            @Parameter(description = "Account ID associated with the user", required = true)
            @PathVariable UUID accountId,
            @RequestBody UserUpdateDTO updateDTO) {
        return ResponseEntity.ok(userService.updateUserProfileByAccountId(accountId, updateDTO));
    }

    // ----------------------------------------------------------------
    // PAGINATION & SEARCH
    // ----------------------------------------------------------------

    @Operation(
        summary = "List all users (paginated)",
        description = "Retrieves a paginated list of users with sorting options.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Paged list of users retrieved successfully",
                         content = @Content(schema = @Schema(implementation = PagedResponseDTO.class)))
        }
    )
    @GetMapping
    public ResponseEntity<PagedResponseDTO<UserProfileDTO>> getAllUsersPaginated(
            @Parameter(description = "Page index (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(userService.getAllUsersPaginated(page, size, sortBy, sortDir));
    }

    @Operation(
        summary = "Search users by name",
        description = "Performs a case-insensitive search across first and last names.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of matching users retrieved successfully",
                         content = @Content(schema = @Schema(implementation = UserProfileDTO.class)))
        }
    )
    @GetMapping("/search")
    public ResponseEntity<List<UserProfileDTO>> searchUsersByName(
            @Parameter(description = "Partial or full name to search", required = true)
            @RequestParam String name) {
        return ResponseEntity.ok(userService.searchUsersByName(name));
    }

    // ----------------------------------------------------------------
    // SYNC ENDPOINTS (triggered by Auth service)
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
        userService.handleEmailUpdateFromAuth(accountId, newEmail);
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
        userService.handleUsernameUpdateFromAuth(accountId, newUsername);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}