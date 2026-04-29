package com.suyos.userservice.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.common.dto.response.PagedResponse;
import com.suyos.common.event.AccountEmailUpdateEvent;
import com.suyos.common.event.AccountUsernameUpdateEvent;
import com.suyos.common.event.UserCreationEvent;
import com.suyos.userservice.dto.request.UserUpdateRequest;
import com.suyos.userservice.dto.response.UserResponse;
import com.suyos.userservice.exception.exceptions.DuplicateEventException;
import com.suyos.userservice.exception.exceptions.UserNotFoundException;
import com.suyos.userservice.mapper.UserMapper;
import com.suyos.userservice.model.ProcessedEvent;
import com.suyos.userservice.model.User;
import com.suyos.userservice.repository.ProcessedEventRepository;
import com.suyos.userservice.repository.UserRepository;
import com.suyos.userservice.specification.UserSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserMapper userMapper;
    
    private final UserRepository userRepository;

    private final ProcessedEventRepository processedEventRepository;

    // ----------------------------------------------------------
    // RETRIEVAL
    // ----------------------------------------------------------

    /**
     * Gets a paginated response of all users, optionally filtered by search
     * text: username, email, first name, or last name.
     * 
     * @param page Zero-based page index
     * @param size Page size
     * @param sortBy Field to sort by
     * @param sortDir Sort direction (asc/desc)
     * @param searchText Optional text to filter by
     * @return Paginated response of all users
     */
    public PagedResponse<UserResponse> getAllUsers(
        int page,
        int size, 
        String sortBy,
        String sortDir,
        String searchText
    ) {
        // Define dynamic sorting rules
        Sort sort = Sort.by(sortBy);
        if ("desc".equalsIgnoreCase(sortDir)) {
            sort = sort.descending();
        }

        // Create search specification
        Specification<User> spec = UserSpecification.filter(
            searchText
        );

        // Create pageable request with dynamic sorting
        Pageable pageable = PageRequest.of(page, size, sort);

        // Find all users filtered by search specification
        Page<User> userPage = userRepository.findAll(spec, pageable);
        
        // Map users responses from users
        List<UserResponse> userResponses = userPage.getContent()
            .stream()
            .map(userMapper::toResponse)
            .toList();

        // Build paginated response of all users
        PagedResponse<UserResponse> pagedResponse = PagedResponse.<UserResponse>builder()
                .content(userResponses)
                .currentPage(userPage.getNumber())
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .size(userPage.getSize())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .build();
        
        // Log users retrieval success
        log.info("event=all_users_retrieved page={} size={} search_text={}", page, size, searchText);

        // Return paginated response of all users
        return pagedResponse;
    }

    /**
     * Gets a user by its ID.
     * 
     * @param id ID of the user to retrieve
     * @return User response
     * @throws UserNotFoundException If user is not found
     */
    public UserResponse getUserById(UUID id) {
        // Find user by ID
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("user_id=" + id));
        
        // Map user response from user
        UserResponse userResponse = userMapper.toResponse(user);

        // Log user retrieval success
        log.info("event=user_retrieved user_id={}", user.getId());
        
        // Return user response
        return userResponse;
    }

    /**
     * Gets a user by its account ID.
     *
     * @param accountId Account ID of the user to retrieve
     * @return User response
     * @throws UserNotFoundException If user is not found
     */
    public UserResponse getUserByAccountId(UUID accountId) {
        // Find user by account ID
        User user = userRepository.findByAccountId(accountId)
                .orElseThrow(() -> new UserNotFoundException("account_id=" + accountId));

        // Map user response from user
        UserResponse userResponse = userMapper.toResponse(user);

        // Log user retrieval success
        log.info("event=user_retrieved user_id={} account_id={}", user.getId(), user.getAccountId());
        
        // Return user response
        return userResponse;
    }

    // ----------------------------------------------------------
    // CREATION
    // ----------------------------------------------------------
    
    /**
     * Creates a new user.
     *
     * @param event User registration data
     * @return Created user response
     */
    public UserResponse createUser(UserCreationEvent event) {
        // Log user creation attempt
        log.info("event=user_creation_attempt account_id={}", event.getAccountId());

        // Ensure no duplicate event processing
        if (processedEventRepository.existsById(event.getId())) {
            throw new DuplicateEventException("event_id=" + event.getId());
        }

        // Create processed event
        ProcessedEvent newEvent = new ProcessedEvent(event.getId(), event.getOccurredAt());

        // Persist created processed event
        processedEventRepository.save(newEvent);

        // Map user from registration data
        User user = userMapper.createFromRequest(event);

        // Set acceptance timestamps
        user.setTermsAcceptedAt(Instant.now());
        user.setPrivacyPolicyAcceptedAt(Instant.now());
        
        // Persist created user
        User createdUser = userRepository.save(user);

        // Log user creation success
        log.info("event=user_created user_id={} account_id={}", createdUser.getId(), createdUser.getAccountId());

        // Map user response from created user
        UserResponse userResponse = userMapper.toResponse(createdUser);

        // Return created user response
        return userResponse;
    }

    // ----------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------

    /**
     * Updates a user by its ID.
     *
     * @param id ID of the user to update
     * @param request User update data
     * @return Updated user response
     * @throws UserNotFoundException If user is not found
     */
    public UserResponse updateUserById(UUID id, UserUpdateRequest request) {
        // Log user update attempt
        log.info("event=user_update_attempt user_id={}", id);

        // Find user by ID
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("user_id=" + id));
        
        // Update user from request
        userMapper.updateFromRequest(user, request);

        // Persist updated user
        User updatedUser = userRepository.save(user);

        // Log user update success
        log.info("event=user_updated user_id={}", updatedUser.getId());

        // Map user response from updated user
        UserResponse userResponse = userMapper.toResponse(updatedUser);

        // Return updated user response
        return userResponse;
    }

    /**
     * Updates a user by its account ID.
     *
     * @param accountId Account ID of the user to update
     * @param request User update data
     * @return Updated user response
     * @throws UserNotFoundException If user is not found
     */
    public UserResponse updateUserByAccountId(UUID accountId, UserUpdateRequest request) {
        // Log user update attempt
        log.info("event=user_update_attempt account_id={}", accountId);

        // Find user by account ID
        User user = userRepository.findByAccountId(accountId)
            .orElseThrow(() -> new UserNotFoundException("account_id=" + accountId));
        
        // Update user from request
        userMapper.updateFromRequest(user, request);

        // Persist updated user
        User updatedUser = userRepository.save(user);

        // Log user update success
        log.info("event=user_updated account_id={}", updatedUser.getAccountId());

        // Map user response from updated user
        UserResponse userResponse = userMapper.toResponse(updatedUser);

        // Return updated user response
        return userResponse;
    }

    // ----------------------------------------------------------
    // SOFT DELETION
    // ----------------------------------------------------------

    /**
     * Soft deletes a user by its account ID.
     *
     * @param accountId Account ID of the user to soft delete
     * @return Soft deleted user response
     * @throws UserNotFoundException If user is not found
     */
    public UserResponse softDeleteUserByAccountId(UUID accountId) {
        // Log user soft deletion attempt
        log.info("event=user_soft_deletion_attempt account_id={}", accountId);

        // Find user by account ID
        User user = userRepository.findByAccountId(accountId)
            .orElseThrow(() -> new UserNotFoundException("account_id=" + accountId));
        
        // Soft delete user
        user.setActive(false);
        user.setSoftDeletedAt(Instant.now());

        // Persist soft deleted user
        User softDeletedUser = userRepository.save(user);

        // Log user soft deletion success
        log.info("event=user_soft_deleted account_id={}", softDeletedUser.getAccountId());

        // Map user response from updated user
        UserResponse userResponse = userMapper.toResponse(softDeletedUser);

        // Return soft deleted user's profile
        return userResponse;
    }

    // ----------------------------------------------------------
    // SYNC OPERATIONS
    // ----------------------------------------------------------

    /**
     * Updates a user's email (triggered by Auth Service).
     * 
     * @param event Account ID associated with the user and new email address
     * @throws UserNotFoundException If user is not found
     */
    public void mirrorEmailUpdate(AccountEmailUpdateEvent event) {
        // Log email mirror attempt
        log.info("event=email_mirror_attempt account_id={}", event.getAccountId());
        
        // Ensure no duplicate event processing
        if (processedEventRepository.existsById(event.getId())) {
            throw new DuplicateEventException("event_id=" + event.getId());
        }

        // Create processed event
        ProcessedEvent newEvent = new ProcessedEvent(event.getId(), event.getOccurredAt());

        // Persist created processed event
        processedEventRepository.save(newEvent);
        
        // Find user by account ID
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
     * Updates a user's username (triggered by Auth Service).
     *
     * @param event Account ID associated with the user and new username
     * @throws UserNotFoundException If user is not found
     */
    public void mirrorUsernameUpdate(AccountUsernameUpdateEvent event) {
        // Log username mirror attempt
        log.info("event=username_mirror_attempt account_id={}", event.getAccountId());

         // Ensure no duplicate event processing
        if (processedEventRepository.existsById(event.getId())) {
            throw new DuplicateEventException("event_id=" + event.getId());
        }

        // Create processed event
        ProcessedEvent newEvent = new ProcessedEvent(event.getId(), event.getOccurredAt());

        // Persist created processed event
        processedEventRepository.save(newEvent);

        // Find user by account ID
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