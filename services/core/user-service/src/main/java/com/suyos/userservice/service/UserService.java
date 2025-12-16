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

import com.suyos.common.dto.response.PagedResponseDTO;
import com.suyos.common.event.AccountEmailUpdateEvent;
import com.suyos.common.event.AccountUsernameUpdateEvent;
import com.suyos.common.event.UserCreationEvent;
import com.suyos.userservice.dto.request.UserUpdateRequestDTO;
import com.suyos.userservice.dto.response.UserProfileDTO;
import com.suyos.userservice.exception.exceptions.UserNotFoundException;
import com.suyos.userservice.mapper.UserMapper;
import com.suyos.userservice.model.ProcessedEvent;
import com.suyos.userservice.model.User;
import com.suyos.userservice.repository.ProcessedEventRepository;
import com.suyos.userservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for user management operations.
 * 
 * <p>Handles user profile retrieval, updates, and profile-related business
 * logic. Focuses on user data management excluding authentication.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
    
    /** Repository for user data access operations */
    private final UserRepository userRepository;
    
    /** Mapper for converting between user entities and DTOs */
    private final UserMapper userMapper;

    /** Repository for processed event data access operations */
    private final ProcessedEventRepository processedEventRepository;

    // ----------------------------------------------------------
    // ADMIN
    // ----------------------------------------------------------

    /**
     * Finds a paginated list of all users.
     * 
     * @param page Page number to search for
     * @param size Size of page
     * @param sortBy Sort
     * @param sortDir Sort direction
     * @return Paginated list of users' profiles
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
        
        // Return paginated list of users' profiles
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
            .orElseThrow(() -> new UserNotFoundException("user_id=" + id));
        
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
        // Log user update attempt
        log.info("event=user_update_attempt user_id={}", id);

        // Look up user by ID
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("user_id=" + id));
        
        // Update user fields using mapper
        userMapper.updateUserFromDTO(userUpdateDTO, user);

        // Persist updated user
        User updatedUser = userRepository.save(user);

        // Log user update success
        log.info("event=user_updated user_id={}", updatedUser.getId());

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
            .orElseThrow(() -> new UserNotFoundException("account_id=" + accountId));

        // Log user found by account ID success
        log.info("event=account_found_by_id account_id={}", accountId);

        // Map user's profile information from user
        UserProfileDTO userProfile = userMapper.toUserProfileDTO(user);
        
        // Return user's profile
        return userProfile;
    }
    
    /**
     * Creates a new user profile.
     *
     * @param event User's registration data
     * @return Created user's profile
     */
    public UserProfileDTO createUser(UserCreationEvent event) {
        // Log user creation attempt
        log.info("event=user_creation_attempt account_id={}", event.getAccountId());

        // Ensure no duplicate event processing
        if (processedEventRepository.existsById(event.getId())) {
            log.info("event=duplicate_event_ignored event_id={}", event.getId());
            return null;
        }

        // Create new processed event
        ProcessedEvent newEvent = new ProcessedEvent(event.getId(), event.getOccurredAt());

        // Persist new processed event
        processedEventRepository.save(newEvent);

        // Map user from registration data
        User user = userMapper.toEntity(event);

        // Set acceptance timestamps
        user.setTermsAcceptedAt(Instant.now());
        user.setPrivacyPolicyAcceptedAt(Instant.now());
        
        // Persist created user
        User createdUser = userRepository.save(user);

        // Log user creation success
        log.info("event=user_created account_id={}", createdUser.getAccountId());

        // Map user's profile from created user
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
        // Log user update attempt
        log.info("event=user_update_attempt account_id={}", accountId);

        // Look up user by account ID
        User user = userRepository.findByAccountId(accountId)
            .orElseThrow(() -> new UserNotFoundException("account_id=" + accountId));
        
        // Update user fields using mapper
        userMapper.updateUserFromDTO(userUpdateDTO, user);

        // Persist updated user
        User updatedUser = userRepository.save(user);

        // Log user update success
        log.info("event=user_updated account_id={}", updatedUser.getAccountId());

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
        // Log user soft-deletion attempt
        log.info("event=user_soft_deletion_attempt account_id={}", accountId);

        // Look up user by account ID
        User user = userRepository.findByAccountId(accountId)
            .orElseThrow(() -> new UserNotFoundException("account_id=" + accountId));
        
        // Soft delete user
        user.setDeleted(true);
        user.setDeletedAt(Instant.now());

        // Persist soft deleted user
        User softDeletedUser = userRepository.save(user);

        // Log user soft deletion success
        log.info("event=user_soft_deleted account_id={}", softDeletedUser.getAccountId());

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
     * @param event Account ID associated with the user and new email address
     * @throws UserNotFoundException If user not found
     */
    public void mirrorEmailUpdate(AccountEmailUpdateEvent event) {
        // Log email mirror attempt
        log.info("event=email_mirror_attempt account_id={}", event.getAccountId());

        // Ensure no duplicate event processing
        if (processedEventRepository.existsById(event.getId())) {
            log.info("event=duplicate_event_ignored event_id={}", event.getId());
            return;
        }

        // Create new processed event
        ProcessedEvent newEvent = new ProcessedEvent(event.getId(), event.getOccurredAt());

        // Persist new processed event
        processedEventRepository.save(newEvent);
        
        // Look up user by account ID
        User user = userRepository.findByAccountId(event.getAccountId())
            .orElseThrow(() -> new UserNotFoundException("account_id=" + event.getAccountId()));
        
        // Update email
        user.setEmail(event.getNewEmail());

        // Log email mirror success
        log.info("event=email_mirrored account_id={}", event.getAccountId());

        // Persist updated user
        userRepository.save(user);
    }

    /**
     * Updates user username (triggered by Auth Service).
     *
     * @param event Account ID associated with the user and new username
     * @throws UserNotFoundException If user not found
     */
    public void mirrorUsernameUpdate(AccountUsernameUpdateEvent event) {
        // Log username mirror attempt
        log.info("event=username_mirror_attempt account_id={}", event.getAccountId());

        // Ensure no duplicate event processing
        if (processedEventRepository.existsById(event.getId())) {
            log.info("event=duplicate_event_ignored event_id={}", event.getId());
            return;
        }

        // Create new processed event
        ProcessedEvent newEvent = new ProcessedEvent(event.getId(), event.getOccurredAt());

        // Persist new processed event
        processedEventRepository.save(newEvent);

        // Look up user by account ID
        User user = userRepository.findByAccountId(event.getAccountId())
            .orElseThrow(() -> new UserNotFoundException("account_id=" + event.getAccountId()));
        
        // Update username
        user.setUsername(event.getNewUsername());

        // Log username mirror success
        log.info("event=username_mirrored account_id={}", event.getAccountId());

        // Persist updated user
        userRepository.save(user);
    }

}