package com.suyos.userservice.unit.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.suyos.common.dto.response.PagedResponseDTO;
import com.suyos.common.event.AccountEmailUpdateEvent;
import com.suyos.common.event.AccountUsernameUpdateEvent;
import com.suyos.common.event.UserCreationEvent;
import com.suyos.userservice.dto.request.UserUpdateRequest;
import com.suyos.userservice.dto.response.UserProfileResponse;
import com.suyos.userservice.exception.exceptions.UserNotFoundException;
import com.suyos.userservice.mapper.UserMapper;
import com.suyos.userservice.model.ProcessedEvent;
import com.suyos.userservice.model.User;
import com.suyos.userservice.repository.ProcessedEventRepository;
import com.suyos.userservice.repository.UserRepository;
import com.suyos.userservice.service.UserService;

/**
 * Unit tests for {@link UserService}.
 *
 * <p>Tests user profile business logic using mocked dependencies
 * to verify service behavior in isolation.</p>
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    /** Mocked user repository */
    @Mock
    private UserRepository userRepository;

    /** Mocked processed event repository */
    @Mock
    private ProcessedEventRepository processedEventRepository;

    /** Mocked user mapper */
    @Mock
    private UserMapper userMapper;

    /** Service under test */
    @InjectMocks
    private UserService userService;

    /** Test user entity */
    private User testUser;

    /** Test user profile */
    private UserProfileResponse testUserProfile;

    /** Test update request */
    private UserUpdateRequest updateRequest;

	/** Updated test user profile */
    private UserProfileResponse updatedTestUserProfile;

    /**
     * Initializes common test data before each test.
     */
    @BeforeEach
    void setUp() {
        // Generate test user's ID and account ID
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        // Build test user
        testUser = User.builder()
                .id(userId)
                .accountId(accountId)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phone("1234567890")
                .build();

        // Build test user's profile
        testUserProfile = UserProfileResponse.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phone("1234567890")
                .build();

        // Build test user's update request
        updateRequest = UserUpdateRequest.builder()
                .firstName("Updated")
                .phone("0987654321")
                .build();
		
	    // Build test user's updated profile
        updatedTestUserProfile = UserProfileResponse.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .firstName("Updated")
                .lastName("Name")
                .phone("0987654321")
                .build();
    }

    // ----------------------------------------------------------
    // LOOKUP
    // ----------------------------------------------------------

    /**
     * Retrieves a user profile by ID successfully.
     */
    @Test
    void findUserById_Success() {
        // Mock user repository to return test user when searched by ID
        when(userRepository.findById(testUser.getId()))
                .thenReturn(Optional.of(testUser));

        // Mock user mapper to return test user profile when mapping test user
        when(userMapper.toUserProfileDTO(testUser))
                .thenReturn(testUserProfile);

        // Call user service to find user by ID
        UserProfileResponse response = userService.findUserById(testUser.getId());

        // Assert expected user profile is returned
        assertThat(response)
				.isNotNull()
				.isEqualTo(testUserProfile);

        // Verify interactions
        verify(userRepository).findById(testUser.getId());
        verify(userMapper).toUserProfileDTO(testUser);
    }

    /**
     * Throws exception when user is not found by ID.
     */
    @Test
    void findUserById_UserNotFound() {
        // Generate random user ID
        UUID id = UUID.randomUUID();

        // Mock user repository to return no user when searched random ID
        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        // Verify user not found exception is thrown
        assertThatThrownBy(() -> userService.findUserById(id))
            	.isInstanceOf(UserNotFoundException.class);
    }

    /**
     * Retrieves a user profile by account ID successfully.
     */
    @Test
    void findUserByAccountId_Success() {
        // Mock user repository to return test user when searched by account ID
        when(userRepository.findByAccountId(testUser.getAccountId()))
                .thenReturn(Optional.of(testUser));

        // Mock user mapper to return test user profile when mapping test user
        when(userMapper.toUserProfileDTO(testUser))
                .thenReturn(testUserProfile);

        // Call service method to find user by account ID
        UserProfileResponse response = userService.findUserByAccountId(testUser.getAccountId());

        // Assert expected user profile is returned
        assertThat(response)
				.isNotNull()
				.isEqualTo(testUserProfile);

        // Verify interactions
        verify(userRepository).findByAccountId(testUser.getAccountId());
		verify(userMapper).toUserProfileDTO(testUser);
    }

    /**
     * Retrieves a paginated list of users successfully.
     */
    @Test
    void findAllUsers_Success() {
		// Build paginated response with test user
        Page<User> page = new PageImpl<>(
                List.of(testUser),
                PageRequest.of(0, 10),
                1
        );

        // Mock user repository to return all test users when searched by pageable
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        // Mock user mapper to return test user profile when mapping test users
        when(userMapper.toUserProfileDTO(testUser))
                .thenReturn(testUserProfile);

        // Call service method to find all users with pagination
        PagedResponseDTO<UserProfileResponse> response =
                userService.findAllUsers(0, 10, "username", "asc");

        // Assert expected user profiles are returned
		assertThat(response).isNotNull();
		assertThat(response.getTotalElements())
				.isEqualTo(1);
		assertThat(response.getContent())
				.hasSize(1)
				.containsExactly(testUserProfile);
    }

    // ----------------------------------------------------------
    // CREATION
    // ----------------------------------------------------------

    /**
     * Creates a new user successfully.
     */
    @Test
    void createUser_Success() {
		// Build user creation event
        UserCreationEvent event = UserCreationEvent.builder()
                .id(UUID.randomUUID().toString())
                .occurredAt(Instant.now())
                .accountId(testUser.getAccountId())
				.username(testUser.getUsername())
				.email(testUser.getEmail())
				.firstName(testUser.getFirstName())
				.lastName(testUser.getLastName())
				.phone(testUser.getPhone())
                .build();

		// Mock processed event repository to check if event has been processed
        when(processedEventRepository.existsById(event.getId()))
                .thenReturn(false);
		
		// Mock user mapper to return test user when mapping user creation event
        when(userMapper.toEntity(event))
                .thenReturn(testUser);

		// Mock user repository to return test user when saving new user
		when(userRepository.save(any(User.class)))
                .thenReturn(testUser);
		
		// Mock user mapper to return test user profile when mapping test user
        when(userMapper.toUserProfileDTO(testUser))
                .thenReturn(testUserProfile);

		// Call service method to create a new user
        UserProfileResponse response = userService.createUser(event);

		// Assert expected user profile is returned
        assertThat(response)
				.isNotNull()
				.isEqualTo(testUserProfile);

		// Assert business logic side effects
		assertThat(testUser.getTermsAcceptedAt())
				.as("Terms acceptance timestamp should be set")
				.isNotNull();
		assertThat(testUser.getPrivacyPolicyAcceptedAt())
				.as("Privacy policy acceptance timestamp should be set")
				.isNotNull();

		// Verify interactions
		verify(processedEventRepository).save(any(ProcessedEvent.class));
		verify(userMapper).toEntity(event);
		verify(userRepository).save(testUser);
		verify(userMapper).toUserProfileDTO(testUser);
    }

    /**
     * Ignores duplicate user creation event.
     */
    @Test
    void createUser_DuplicateEventIgnored() {
		// Build user creation event
        UserCreationEvent event = UserCreationEvent.builder()
                .id(UUID.randomUUID().toString())
                .occurredAt(Instant.now())
                .accountId(testUser.getAccountId())
				.username(testUser.getUsername())
				.email(testUser.getEmail())
				.firstName(testUser.getFirstName())
				.lastName(testUser.getLastName())
				.phone(testUser.getPhone())
                .build();

		// Mock processed event repository to indicate event has already been processed
        when(processedEventRepository.existsById(event.getId()))
                .thenReturn(true);

		// Call service method to create a new user from duplicate event
        UserProfileResponse response = userService.createUser(event);

		// Assert null response for duplicate event
		assertThat(response)
				.isNull();

		// Verify interactions
        verify(userRepository, never()).save(any());
    }

    // ----------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------

    /**
     * Updates a user by ID successfully.
     */
    @Test
    void updateUserById_Success() {
		// Mock user repository to return test user when searched by ID
        when(userRepository.findById(testUser.getId()))
                .thenReturn(Optional.of(testUser));
		
		// Mock user mapper to update test user by ID
		doAnswer(invocation -> {
			UserUpdateRequest request = invocation.getArgument(0);
			User user = invocation.getArgument(1);

			user.setFirstName(request.getFirstName());
			user.setPhone(request.getPhone());

			return null;
		}).when(userMapper).updateUserFromDTO(any(), any());
			
        // Mock user repository to return updated test user when updating test user
		when(userRepository.save(testUser))
                .thenReturn(testUser);
		
		// Mock user mapper to return updated test user profile when mapping test user
        when(userMapper.toUserProfileDTO(testUser))
                .thenReturn(updatedTestUserProfile);

		// Call service method to update user
        UserProfileResponse response = userService.updateUserById(testUser.getId(), updateRequest);

		// Assert expected updated user profile is returned
        assertThat(response)
				.isNotNull()
				.isEqualTo(updatedTestUserProfile);
		
		// Verify interactions
		verify(userRepository).findById(testUser.getId());
		verify(userMapper).updateUserFromDTO(updateRequest, testUser);
		verify(userRepository).save(testUser);
		verify(userMapper).toUserProfileDTO(testUser);
    }

    /**
     * Throws exception when updating non-existing user.
     */
    @Test
    void updateUserById_UserNotFound() {
		// Generate random user ID
        UUID id = UUID.randomUUID();

        // Mock user repository to return no user when searched random ID
        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

		// Verify user not found exception is thrown
        assertThatThrownBy(() -> userService.updateUserById(id, updateRequest))
            	.isInstanceOf(UserNotFoundException.class);
    }

    // ----------------------------------------------------------
    // SOFT-DELETION
    // ----------------------------------------------------------

    /**
     * Soft-deletes a user by account ID.
     */
    @Test
    void softDeleteUserByAccountId_Success() {
		// Mock user repository to return test user when searched by account ID
        when(userRepository.findByAccountId(testUser.getAccountId()))
                .thenReturn(Optional.of(testUser));

		// Mock user repository to return soft-deleted test user when soft-deleting test user
        when(userRepository.save(testUser))
                .thenReturn(testUser);
		
		// Mock user mapper to return test user profile when mapping test user
        when(userMapper.toUserProfileDTO(testUser))
                .thenReturn(testUserProfile);
		
		// Call service method to soft-delete user by account ID
        UserProfileResponse response = userService.softDeleteUserByAccountId(testUser.getAccountId());
		
		// Assert user profile is returned
		assertThat(response).isNotNull();

		// Assert business logic side effects
        assertThat(testUser.getDeletedAt())
				.as("deletedAt should be set when user is soft-deleted")
				.isNotNull();
    }

    // ----------------------------------------------------------
    // SYNC OPERATIONS
    // ----------------------------------------------------------

    /**
     * Mirrors email update successfully.
     */
    @Test
    void mirrorEmailUpdate_Success() {
        AccountEmailUpdateEvent event = new AccountEmailUpdateEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                testUser.getAccountId(),
                "new@mail.com"
        );

        when(processedEventRepository.existsById(event.getId()))
                .thenReturn(false);
        when(userRepository.findByAccountId(testUser.getAccountId()))
                .thenReturn(Optional.of(testUser));

        userService.mirrorEmailUpdate(event);

        assertThat(testUser.getEmail()).isEqualTo("new@mail.com");
        verify(userRepository).save(testUser);
    }

    /**
     * Ignores duplicate email update event.
     */
    @Test
    void mirrorEmailUpdate_DuplicateEventIgnored() {
        AccountEmailUpdateEvent event = new AccountEmailUpdateEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                testUser.getAccountId(),
                "new@mail.com"
        );

        when(processedEventRepository.existsById(event.getId()))
                .thenReturn(true);

        userService.mirrorEmailUpdate(event);

        verify(userRepository, never()).save(any());
    }

    /**
     * Mirrors username update successfully.
     */
    @Test
    void mirrorUsernameUpdate_Success() {
        AccountUsernameUpdateEvent event = new AccountUsernameUpdateEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                testUser.getAccountId(),
                "newusername"
        );

        when(processedEventRepository.existsById(event.getId()))
                .thenReturn(false);
        when(userRepository.findByAccountId(testUser.getAccountId()))
                .thenReturn(Optional.of(testUser));

        userService.mirrorUsernameUpdate(event);

        assertThat(testUser.getUsername()).isEqualTo("newusername");
        verify(userRepository).save(testUser);
    }
	
}