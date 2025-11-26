package com.suyos.userservice.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.userservice.dto.request.UserCreationRequestDTO;
import com.suyos.userservice.dto.request.UserUpdateRequestDTO;
import com.suyos.userservice.dto.response.UserProfileDTO;
import com.suyos.userservice.exception.exceptions.UserNotFoundException;
import com.suyos.common.dto.response.PagedResponseDTO;
import com.suyos.userservice.mapper.UserMapper;
import com.suyos.userservice.model.User;
import com.suyos.userservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service for user profile management operations.
 * 
 * <p>Handles user profile retrieval, updates, and profile-related business
 * logic. Focuses on user data management excluding authentication.</p>
 *
 * @author Joel Salazar
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    /** Repository for user data access operations */
    private final UserRepository userRepository;
    
    /** Mapper for converting between user entities and DTOs */
    private final UserMapper userMapper;

    // ----------------------------------------------------------
    // ADMIN
    // ----------------------------------------------------------

    /**
     * Finds all users paginated.
     * 
     * @param page Page number to search for
     * @param size Size of page
     * @param sortBy Sort
     * @param sortDir Sort direction
     * @return All accounts' information
     */
    public PagedResponseDTO<UserProfileDTO> findAllUsers(int page, int size, 
        String sortBy, String sortDir) {
        // Define dynamic sorting rules
        Sort sort = Sort.by(sortBy);
        if ("desc".equalsIgnoreCase(sortDir)) {
            sort = sort.descending();
        }

        // Create pageable request with dynamic sorting
        Pageable pageable = PageRequest.of(page, size, sort);

        // Look up all accounts paginated
        Page<User> userPage = userRepository.findAll(pageable);
        
        // Map accounts' information from accounts
        List<UserProfileDTO> userProfiles = userPage.getContent()
            .stream()
            .map(userMapper::toUserProfileDTO)
            .toList();

        // Build paginated response with all users' profiles
        PagedResponseDTO<UserProfileDTO> response = PagedResponseDTO.<UserProfileDTO>builder()
            .content(userProfiles)
            .currentPage(userPage.getNumber())
            .totalPages(userPage.getTotalPages())
            .totalElements(userPage.getTotalElements())
            .size(userPage.getSize())
            .first(userPage.isFirst())
            .last(userPage.isLast())
            .build();
        
        // Return all users' profiles paginated
        return response;
    }

    /**
     * Find a user by ID.
     * 
     * @param id User's ID to search for
     * @return User's profile information
     * @throws UserNotFoundException If user not found
     */
    @Transactional(readOnly = true)
    public UserProfileDTO findUserById(UUID id) {
        // Look up user by ID
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("ID: " + id));
        
        // Map user's profile information from user
        UserProfileDTO userProfile = userMapper.toUserProfileDTO(user);
        
        // Return user's profile
        return userProfile;
    }

    /**
     * Updates user profile by ID.
     *
     * @param id User's ID to update
     * @param userUpdateDTO User's update data
     * @return Updated user's profile
     * @throws UserNotFoundException If user not found
     */
    public UserProfileDTO updateUserById(UUID id, UserUpdateRequestDTO userUpdateDTO) {
        // Look up user by ID
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("ID: " + id));
        
        // Update user fields using mapper
        userMapper.updateUserFromDTO(userUpdateDTO, user);

        // Persist updated user
        User updatedUser = userRepository.save(user);

        // Map user's profile information from updated user
        UserProfileDTO userProfile = userMapper.toUserProfileDTO(updatedUser);

        // Return updated user's profile
        return userProfile;
    }

    /**
     * Searches for users by their first or last name.
     * 
     * <p>Performs a case-insensitive partial match on both first name and last 
     * name fields, returning all matching users as profile DTOs.</p>
     *
     * @param name Name fragment to search for
     * @return List of matching users' profile
     */
    @Transactional(readOnly = true)
    public List<UserProfileDTO> searchUsersByName(String name) {
        // 
        return userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name)
            .stream()
            .map(userMapper::toUserProfileDTO)
            .collect(Collectors.toList());
    }

    // ----------------------------------------------------------
    // USER PROFILE MANAGEMENT
    // ----------------------------------------------------------

    /**
     * Retrieves user profile by account ID.
     *
     * @param accountId Account's ID associated with the user
     * @return User's profile
     * @throws UserNotFoundException If user not found
     */
    @Transactional(readOnly = true)
    public UserProfileDTO findUserByAccountId(UUID accountId) {
        // Look up user by account ID
        User user = userRepository.findByAccountId(accountId)
            .orElseThrow(() -> new UserNotFoundException("account ID: " + accountId));
        
        // Map user's profile information from user
        UserProfileDTO userProfile = userMapper.toUserProfileDTO(user);
        
        // Return user's profile
        return userProfile;
    }
    
    /**
     * Creates a new user profile.
     *
     * @param accountId Account ID associated with the user
     * @param username Username of the user
     * @param email Email of the user
     * @param request User's registration data
     * @return Created user's profile
     */
    public UserProfileDTO createUser(UserCreationRequestDTO request) {
        // Map user from registration data
        User user = userMapper.toEntity(request);
        
        // Mirror fields managed by Auth microservice
        user.setAccountId(request.getAccountId());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());

        // Set acceptance timestamps
        user.setTermsAcceptedAt(Instant.now());
        user.setPrivacyPolicyAcceptedAt(Instant.now());
        
        // Persist created user
        User createdUser = userRepository.save(user);

        // Map user's profile information from created user
        UserProfileDTO userProfile = userMapper.toUserProfileDTO(createdUser);

        // Return created user's profile
        return userProfile;
    }

    /**
     * Updates user profile by account ID.
     *
     * @param accountId Account ID associated with the user
     * @param userUpdateDTO User's update data
     * @return Updated user's profile information
     * @throws UserNotFoundException If user not found
     */
    public UserProfileDTO updateUserByAccountId(UUID accountId, UserUpdateRequestDTO userUpdateDTO) {
        // Look up user by account ID
        User user = userRepository.findByAccountId(accountId)
            .orElseThrow(() -> new UserNotFoundException("account ID: " + accountId));
        
        // Update user fields using mapper
        userMapper.updateUserFromDTO(userUpdateDTO, user);

        // Persist updated user
        User updatedUser = userRepository.save(user);

        // Map user's profile information from updated user
        UserProfileDTO userProfile = userMapper.toUserProfileDTO(updatedUser);

        // Return updated user's profile
        return userProfile;
    }

    /**
     * Deletes user profile by account ID.
     *
     * @param accountId Account ID associated with the user
     * @param userUpdateDTO User's update data
     * @return Updated user's profile information
     * @throws UserNotFoundException If user not found
     */
    public UserProfileDTO softDeleteUserByAccountId(UUID accountId) {
        // Look up user by account ID
        User user = userRepository.findByAccountId(accountId)
            .orElseThrow(() -> new UserNotFoundException("account ID: " + accountId));
        
        // Soft delete user
        user.setDeleted(true);
        user.setDeletedAt(Instant.now());

        // Persist soft deleted user
        User softDeletedUser = userRepository.save(user);

        // Map user's profile information from updated user
        UserProfileDTO userProfile = userMapper.toUserProfileDTO(softDeletedUser);

        // Return soft deleted user's profile
        return userProfile;
    }

    // ----------------------------------------------------------
    // SYNC OPERATIONS
    // ----------------------------------------------------------

    /**
     * Updates user email (triggered by Auth Service).
     * 
     * @param accountId Account ID associated with the user
     * @param newEmail New email address
     * @throws UserNotFoundException If user not found
     */
    public void mirrorEmailUpdate(UUID accountId, String newEmail) {
        // Look up user by account ID
        User user = userRepository.findByAccountId(accountId)
            .orElseThrow(() -> new UserNotFoundException("account ID: " + accountId));
        
        // Update email
        user.setEmail(newEmail);

        // Persist updated user
        userRepository.save(user);
    }

    /**
     * Updates user username (triggered by Auth Service).
     *
     * @param accountId Account ID associated with the user
     * @param newUsername New username
     * @throws UserNotFoundException If user not found
     */
    public void mirrorUsernameUpdate(UUID accountId, String newUsername) {
        // Look up user by account ID
        User user = userRepository.findByAccountId(accountId)
            .orElseThrow(() -> new UserNotFoundException("account ID: " + accountId));
        
        // Update username
        user.setUsername(newUsername);

        // Persist updated user
        userRepository.save(user);
    }

}