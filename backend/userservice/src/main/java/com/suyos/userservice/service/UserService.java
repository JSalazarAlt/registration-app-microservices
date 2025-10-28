package com.suyos.userservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.userservice.dto.PagedResponseDTO;
import com.suyos.userservice.dto.UserProfileDTO;
import com.suyos.userservice.dto.UserRegistrationDTO;
import com.suyos.userservice.dto.UserUpdateDTO;
import com.suyos.userservice.mapper.UserMapper;
import com.suyos.userservice.model.User;
import com.suyos.userservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service for user profile management operations.
 * 
 * <p>Handles user profile retrieval, updates, and profile-related business logic.
 * Focuses on user data management excluding authentication operations.</p>
 *
 * @author Joel Salazar
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    /** Repository for user data access operations */
    private final UserRepository userRepository;
    
    /** Mapper for converting between entities and DTOs */
    private final UserMapper userMapper;

    // ----------------------------------------------------------
    // ADMIN / INTERNAL OPERATIONS (use userId)
    // ----------------------------------------------------------

    /**
     * Retrieves an existing user's profile using their ID.
     * 
     * @param id User's ID
     * @return User's profile information
     * @throws RuntimeException If user not found
     */
    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfileById(UUID id) {
        // Fetch if there is an existing user for the given ID
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found for id: " + id));
        
        // Return the user's profile DTO
        return userMapper.toProfileDTO(user);
    }

    /**
     * Updates an existing user's profile using their ID and DTO to update.
     *
     * @param id User's ID
     * @param userUpdateDTO DTO to update the user's profile
     * @return User's updated profile DTO
     */
    public UserProfileDTO updateUserProfileById(UUID id, UserUpdateDTO userUpdateDTO) {
        // Fetch if there is an existing user for the given ID
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found for id: " + id));
        
        // Update the user using Mapstruct, ignoring nulls and sensitive fields. 
        // Replaces all the manual null checks like: 
        // if(userUpdateDTO.getX() != null) { existingUser.setX(userUpdateDTO.getX()); }
        userMapper.updateUserFromDTO(userUpdateDTO, user);

        // Persist the updated user
        User savedUser = userRepository.save(user);

        // Return the user's updated profile DTO
        return userMapper.toProfileDTO(savedUser);
    }

    /**
     * Checks if a user exists by their ID.
     * 
     * @param id User's ID
     * @return True if user exists, False otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return userRepository.existsById(id);
    }

    // ----------------------------------------------------------
    // USER-FACING OPERATIONS (use accountId)
    // ----------------------------------------------------------

    /**
     * Retrieves an existing user's profile using their account ID.
     *
     * @param accountId Account ID associated with the user
     * @return User's profile DTO
     * @throws RuntimeException If the user is not found
     */
    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfileByAccountId(UUID accountId) {
        // Fetch if there is an existing user for the given account ID
        User user = userRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("User not found for accountId: " + accountId));
        
        // Return the user's profile DTO
        return userMapper.toProfileDTO(user);
    }
    
    /**
     * Creates a new user's using their account ID and DTO to update.
     *
     * @param accountId Account ID associated with the user
     * @param username Username of the new user
     * @param email Email of the new user
     * @param userRegistrationDTO DTO to create the user's profile
     * @return User's updated profile DTO
     */
    public UserProfileDTO createUser(UUID accountId, String username, 
        String email, UserRegistrationDTO userRegistrationDTO) {
        // Map profile-specific fields for the new user
        User user = userMapper.toEntity(userRegistrationDTO);
        
        // Mirror Auth-managed fields
        user.setAccountId(accountId);
        user.setEmail(email);
        user.setUsername(username);

        // Set acceptance timestamps
        user.setTermsAcceptedAt(LocalDateTime.now());
        user.setPrivacyPolicyAcceptedAt(LocalDateTime.now());
        
        // Persist the created user
        User savedUser = userRepository.save(user);

        // Return the user's created profile DTO
        return userMapper.toProfileDTO(savedUser);
    }

    /**
     * Updates an existing user's profile using their account ID and DTO to update.
     *
     * @param accountId Account ID associated with the user
     * @param userUpdateDTO DTO to update the user's profile
     * @return User's updated profile DTO
     */
    public UserProfileDTO updateUserProfileByAccountId(UUID accountId, UserUpdateDTO userUpdateDTO) {
        // Fetch if there is an existing user for the given account ID
        User user = userRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("User not found for accountId: " + accountId));
        
        // Update the user using Mapstruct, ignoring nulls and sensitive fields. 
        // Replaces all the manual null checks like: 
        // if(userUpdateDTO.getX() != null) { existingUser.setX(userUpdateDTO.getX()); }
        userMapper.updateUserFromDTO(userUpdateDTO, user);

        // Persist the updated user
        User savedUser = userRepository.save(user);

        // Return the user's updated profile DTO
        return userMapper.toProfileDTO(savedUser);
    }

    // ----------------------------------------------------------
    // SYNC OPERATIONS (triggered by Auth service)
    // ----------------------------------------------------------

    /**
     * Updates a user's email when notified by the Auth Service.
     * 
     * @param accountId Account ID associated with the user
     * @param newEmail New email
     */
    public void handleEmailUpdateFromAuth(UUID accountId, String newEmail) {
        // Fetch if there is an existing user for the given account ID
        User user = userRepository.findByAccountId(accountId)
            .orElseThrow(() -> new RuntimeException("User not found for accountId: " + accountId));
        
        // Update the user's email
        user.setEmail(newEmail);

        // Persist the updated user
        userRepository.save(user);
    }

    /**
     * Updates a user's username when notified by the Auth Service.
     *
     * @param accountId Account ID associated with the user
     * @param newUsername New username
     */
    public void handleUsernameUpdateFromAuth(UUID accountId, String newUsername) {
        // Fetch if there is an existing user for the given account ID
        User user = userRepository.findByAccountId(accountId)
            .orElseThrow(() -> new RuntimeException("User not found for accountId: " + accountId));
        
        // Update the user's username and save the user
        user.setUsername(newUsername);

        // Persist the updated user
        userRepository.save(user);
    }

    // ----------------------------------------------------------
    // PAGINATION & SEARCH
    // ----------------------------------------------------------

    /**
     * Retrieves users with pagination, sorting, and optional filtering.
     * 
     * <p>Provides efficient data access for large datasets by implementing server
     * side pagination with optional filters. Results are sorted by the specified 
     * field and direction.</p>
     * 
     * @param page Zero-based page index
     * @param size Number of records per page
     * @param sortBy Field name to sort by
     * @param sortDir Sort direction ("asc" or "desc")
     * @return PagedResponse containing expense DTOs and pagination metadata
     */
    public PagedResponseDTO<UserProfileDTO> getAllUsersPaginated(int page, int size, 
        String sortBy, String sortDir) {
        // Create pageable request with dynamic sorting
        Sort sort = Sort.by(sortBy);
        if ("desc".equalsIgnoreCase(sortDir)) {
            sort = sort.descending();
        }
        Pageable pageable = PageRequest.of(page, size, sort);

        // Apply conditional filtering based on provided parameters (user-specific)
        Page<User> userPage;
        userPage = userRepository.findAll(pageable);
        
        // Convert entities to DTOs for API response
        List<UserProfileDTO> users = userPage.getContent()
            .stream()
            .map(userMapper::toProfileDTO)
            .toList();
        
        // Build paginated response with metadata
        return PagedResponseDTO.<UserProfileDTO>builder()
            .content(users)
            .currentPage(userPage.getNumber())
            .totalPages(userPage.getTotalPages())
            .totalElements(userPage.getTotalElements())
            .size(userPage.getSize())
            .first(userPage.isFirst())
            .last(userPage.isLast())
            .build();
    }

    /**
     * Searches for users by their first or last name.
     * 
     * <p>Performs a case-insensitive partial match on both first name and last 
     * name fields, returning all matching users as profile DTOs.</p>
     *
     * @param name Name fragment to search for
     * @return List of matching users as {@link UserProfileDTO}s
     */
    @Transactional(readOnly = true)
    public List<UserProfileDTO> searchUsersByName(String name) {
        return userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name)
            .stream()
            .map(userMapper::toProfileDTO)
            .collect(Collectors.toList());
    }

}