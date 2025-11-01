package com.suyos.userservice.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suyos.userservice.dto.UserProfileDTO;
import com.suyos.userservice.dto.UserRegistrationDTO;
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

    /** Service for handling user profile logic */
    private final UserService userService;

    /**
     * Creates a new user profile from Auth Service request.
     * 
     * @param accountId Account ID from Auth Service
     * @param username Username from Auth Service
     * @param email Email from Auth Service
     * @param userRegistrationDTO Optional user profile data
     * @return Created user profile information
     */
    @PostMapping
    public UserProfileDTO createUser(
            @RequestParam UUID accountId,
            @RequestParam String username,
            @RequestParam String email,
            @RequestBody(required = false) UserRegistrationDTO userRegistrationDTO) {
        
        return userService.createUser(accountId, username, email, userRegistrationDTO);
    }
    
}