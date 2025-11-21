package com.suyos.userservice.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.userservice.dto.request.UserCreationRequestDTO;
import com.suyos.userservice.dto.response.UserProfileDTO;
import com.suyos.userservice.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * Internal REST controller for interservice user operations.
 *
 * <p>Handles user creation requests from Auth Service during account
 * registration. Not exposed to external clients.</p>
 *
 * @author Joel Salazar
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    /** Service for user business logic */
    private final UserService userService;

    /**
     * Creates a new user profile from Auth Service request.
     * 
     * @param request User's registration data
     * @return Created user profile
     */
    @PostMapping
    public UserProfileDTO createUser(
            @RequestBody UserCreationRequestDTO request) {
        // Create user profile
        UserProfileDTO userProfile = userService.createUser(request);
        
        // Return created user's profile
        return userProfile;
    }
    
}