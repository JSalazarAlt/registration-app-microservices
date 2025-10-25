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

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @PostMapping
    public UserProfileDTO createUser(
            @RequestParam UUID accountId,
            @RequestParam String username,
            @RequestParam String email,
            @RequestBody(required = false) UserRegistrationDTO userRegistrationDTO) {
        
        return userService.createUser(accountId, username, email, userRegistrationDTO);
    }
    
}