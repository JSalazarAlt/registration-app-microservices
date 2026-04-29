package com.suyos.userservice.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.common.dto.response.PagedResponse;
import com.suyos.userservice.dto.request.UserUpdateRequest;
import com.suyos.userservice.dto.response.UserResponse;
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
    name = "Admin User Management",
    description = "Admin operations for managing user profiles"
)
public class AdminUserController {

    /** Service for user business logic */
    private final UserService userService;

    // ----------------------------------------------------------------
    // LOOKUP
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
                content = @Content(schema = @Schema(implementation = PagedResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or sort parameters"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping
    public ResponseEntity<PagedResponse<UserResponse>> getAllUsers(
        @Parameter(description = "Page index (0-based)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
        @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
        @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "desc") String sortDir,
        @Parameter(description = "Text to filter by") @RequestParam(required = false) String searchText
    ) {
        // Find paginated list of users' profiles
        PagedResponse<UserResponse> users = userService.getAllUsers(
            page,
            size,
            sortBy, 
            sortDir,
            searchText
        );

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
                content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
        @Parameter(description = "User's unique ID", required = true)
        @PathVariable UUID id
    ) {
        // Find user's profile by id
        UserResponse userProfile = userService.getUserById(id);

        // Return user's profile with "200 OK" status
        return ResponseEntity.ok(userProfile);
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

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
                content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUserById(
        @Parameter(description = "User's unique ID", required = true)
        @PathVariable UUID id,
        @RequestBody UserUpdateRequest userUpdateDTO
    ) {
        // Update user's profile by ID
        UserResponse userProfile = userService.updateUserById(id, userUpdateDTO);
        
        // Return updated user's profile with "200 OK" status
        return ResponseEntity.ok(userProfile);
    }

}