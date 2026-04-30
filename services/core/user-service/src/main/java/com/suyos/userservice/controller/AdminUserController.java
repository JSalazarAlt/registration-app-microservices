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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.annotation.Secured;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(
    name = "Admin User Management",
    description = "Admin operations for managing user profiles"
)
public class AdminUserController {

    private final UserService userService;

    // ----------------------------------------------------------------
    // RETRIEVAL
    // ----------------------------------------------------------------

    /**
     * Gets a paginated response of all users, optionally filtered by search
     * text: username, email, first name, or last name.
     *
     * @param page Zero-based page index
     * @param size Page size
     * @param sortBy Field to sort by
     * @param sortDir Sort direction (asc/desc)
     * @param searchText Optional text to filter by
     * @return Paginated response of all users with "200 OK" status
     */
    @Secured("ROLE_ADMIN")
    @Operation(
        description = "Gets a paginated response of all users optionally filtered by search text.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or sort parameters"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
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
        // Get paginated response of all users
        PagedResponse<UserResponse> users = userService.getAllUsers(
            page,
            size,
            sortBy, 
            sortDir,
            searchText
        );

        // Return paginated response of all users with "200 OK" status
        return ResponseEntity.ok(users);
    }

    /**
     * Gets a user by its ID.
     *
     * @param id ID of the user to retrieve
     * @return User response with "200 OK" status
     */
    @Secured("ROLE_ADMIN")
    @Operation(
        description = "Gets a user by its ID.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
        @Parameter(description = "ID of the user to retrieve", required = true)
        @PathVariable UUID id
    ) {
        // Get user by ID
        UserResponse userResponse = userService.getUserById(id);

        // Return user response with "200 OK" status
        return ResponseEntity.ok(userResponse);
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    /**
     * Updates a user by its ID.
     *
     * @param id ID of the user to update
     * @return Updated user response with "200 OK" status
     */
    @Secured("ROLE_ADMIN")
    @Operation(
        description = "Updates a user by its ID.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUserById(
        @Parameter(description = "ID of the user to update", required = true) @PathVariable UUID id,
        @Parameter(description = "Updated user information") @RequestBody UserUpdateRequest request
    ) {
        // Update user by ID
        UserResponse userResponse = userService.updateUserById(id, request);
        
        // Return updated user response with "200 OK" status
        return ResponseEntity.ok(userResponse);
    }

}