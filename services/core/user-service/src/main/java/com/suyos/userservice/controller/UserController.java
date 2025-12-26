package com.suyos.userservice.controller;

import java.util.List;
import java.util.UUID;

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

import com.suyos.common.dto.response.PagedResponseDTO;
import com.suyos.userservice.dto.request.UserUpdateRequest;
import com.suyos.userservice.dto.response.UserProfileResponse;
import com.suyos.userservice.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.annotation.Secured;

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
    // ADMIN
    // ----------------------------------------------------------------

    /**
     * Retrieves a paginated list of all users.
     *
     * @param page Zero-based page index
     * @param size Page size
     * @param sortBy Field to sort by
     * @param sortDir Sort direction (asc/desc)
     * @return Paginated list of users' profiles with "200 OK" status
     */
    @Secured("ROLE_ADMIN")
    @Operation(
        summary = "Get all users",
        description = "Retrieves a paginated list of users with sorting options",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Users retrieved successfully",
                content = @Content(schema = @Schema(implementation = PagedResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or sort parameters"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping
    public ResponseEntity<PagedResponseDTO<UserProfileResponse>> getAllUsers(
        @Parameter(description = "Page index (0-based)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
        @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
        @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "desc") String sortDir
    ) {
        // Find paginated list of users' profiles
        PagedResponseDTO<UserProfileResponse> users = userService.findAllUsers(
            page, size, sortBy, sortDir);

        // Return paginated list of users' profiles with "200 OK" status
        return ResponseEntity.ok(users);
    }

    /**
     * Retrieves a user by ID.
     *
     * @param id User's ID to search for
     * @return User's profile with "200 OK" status
     */
    @Secured("ROLE_ADMIN")
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves a user's profile using their ID.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "User retrieved successfully",
                content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserById(
        @Parameter(description = "User's unique ID", required = true)
        @PathVariable UUID id
    ) {
        // Find user's profile by id
        UserProfileResponse userProfile = userService.findUserById(id);

        // Return user's profile with "200 OK" status
        return ResponseEntity.ok(userProfile);
    }

    /**
     * Updates a user by ID.
     *
     * @param id User's ID to update
     * @return User's profile with "200 OK" status
     */
    @Secured("ROLE_ADMIN")
    @Operation(
        summary = "Update user by ID",
        description = "Updates a user's profile using their ID.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "User profile updated successfully",
                content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<UserProfileResponse> updateUserById(
        @Parameter(description = "User's unique ID", required = true)
        @PathVariable UUID id,
        @RequestBody UserUpdateRequest userUpdateDTO
    ) {
        // Update user's profile by ID
        UserProfileResponse userProfile = userService.updateUserById(id, userUpdateDTO);
        
        // Return updated user's profile with "200 OK" status
        return ResponseEntity.ok(userProfile);
    }

    /**
     * Searches users by a partial or full name match.
     *
     * @param name Partial or full first/last name to search
     * @return List of user profiles matching the query
     */
    @Secured("ROLE_ADMIN")
    @Operation(
        summary = "Search users by name",
        description = "Performs a case-insensitive search across first and last names.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Users retrieved successfully",
                content = @Content(schema = @Schema(implementation = PagedResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or sort parameters"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/search")
    public ResponseEntity<List<UserProfileResponse>> searchUsersByName(
        @Parameter(description = "Partial or full name to search", required = true)
        @RequestParam String name
    ) {
        // Search users by name
        List<UserProfileResponse> users = userService.findUsersByName(name);
        
        // Return matching users' profile information with "200 OK" status
        return ResponseEntity.ok(users);
    }

    // ----------------------------------------------------------------
    // USER MANAGEMENT
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