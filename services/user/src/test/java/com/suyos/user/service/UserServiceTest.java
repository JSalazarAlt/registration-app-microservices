package com.suyos.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.suyos.common.event.AccountEmailUpdateEvent;
import com.suyos.common.event.AccountUsernameUpdateEvent;
import com.suyos.user.dto.request.UserUpdateRequestDTO;
import com.suyos.user.dto.response.UserProfileDTO;
import com.suyos.user.mapper.UserMapper;
import com.suyos.user.model.User;
import com.suyos.user.repository.UserRepository;

/**
 * Unit tests for UserService.
 *
 * <p>Tests user profile business logic using mocked dependencies
 * to verify service behavior.</p>
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    /** Mocked user repository */
    @Mock
    private UserRepository userRepository;
    
    /** Mocked user mapper */
    @Mock
    private UserMapper userMapper;
    
    /** User service under test with injected mocks */
    @InjectMocks
    private UserService userService;
    
    /** Test user entity */
    private User testUser;
    
    /** Test user profile DTO */
    private UserProfileDTO testUserDTO;
    
    /** Test user update DTO */
    private UserUpdateRequestDTO updateDTO;

    /**
     * Sets up test data before each test.
     */
    @BeforeEach
    void setUp() {
        // Generate test IDs
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
                .createdAt(Instant.now())
                .build();
        
        // Build test user profile DTO
        testUserDTO = UserProfileDTO.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phone("1234567890")
                .build();
        
        // Build test update DTO
        updateDTO = UserUpdateRequestDTO.builder()
                .firstName("Updated")
                .lastName("Name")
                .phone("0987654321")
                .build();
    }

    /**
     * Tests successful user profile retrieval by ID.
     * 
     * <p>Verifies that user profile is retrieved and mapped correctly
     * when user exists in database.</p>
     */
    @Test
    void getUserById_Success() {
        // Mock repository to return test user
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(userMapper.toUserProfileDTO(any(User.class))).thenReturn(testUserDTO);

        // Get user profile
        UserProfileDTO result = userService.findUserById(testUser.getId());

        // Verify user profile was retrieved successfully
        assertNotNull(result);
        assertEquals(testUserDTO.getUsername(), result.getUsername());
        assertEquals(testUserDTO.getEmail(), result.getEmail());
        verify(userRepository).findById(testUser.getId());
        verify(userMapper).toUserProfileDTO(testUser);
    }

    /**
     * Tests user profile retrieval with non-existing ID.
     * 
     * <p>Verifies that exception is thrown when user does not exist
     * in database.</p>
     */
    @Test
    void getUserById_UserNotFound() {
        // Mock repository to return empty
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Attempt to get user profile and expect exception
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.findUserById(nonExistentId));
        assertTrue(exception.getMessage().contains("User not found"));
    }

    /**
     * Tests successful user profile update by ID.
     * 
     * <p>Verifies that user profile is updated and saved correctly
     * when valid update data is provided.</p>
     */
    @Test
    void updateUserById_Success() {
        // Mock dependencies for successful update
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toUserProfileDTO(any(User.class))).thenReturn(testUserDTO);

        // Update user profile
        UserProfileDTO result = userService.updateUserById(testUser.getId(), updateDTO);

        // Verify user profile was updated successfully
        assertNotNull(result);
        verify(userRepository).findById(testUser.getId());
        verify(userMapper).updateUserFromDTO(updateDTO, testUser);
        verify(userRepository).save(testUser);
        verify(userMapper).toUserProfileDTO(testUser);
    }

    /**
     * Tests successful user profile retrieval by account ID.
     * 
     * <p>Verifies that user profile is retrieved correctly when
     * searching by account ID.</p>
     */
    @Test
    void getUserByAccountId_Success() {
        // Mock repository to return test user
        when(userRepository.findByAccountId(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(userMapper.toUserProfileDTO(any(User.class))).thenReturn(testUserDTO);

        // Get user profile by account ID
        UserProfileDTO result = userService.findUserByAccountId(testUser.getAccountId());

        // Verify user profile was retrieved successfully
        assertNotNull(result);
        assertEquals(testUserDTO.getUsername(), result.getUsername());
        verify(userRepository).findByAccountId(testUser.getAccountId());
    }

    /**
     * Tests user profile update with non-existing ID.
     */
    @Test
    void updateUserById_UserNotFound() {
        // Mock repository to return empty
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Attempt to update user profile and expect exception
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.updateUserById(nonExistentId, updateDTO));
        assertTrue(exception.getMessage().contains("User not found"));
    }

    /**
     * Tests user profile retrieval by account ID with non-existing account.
     */
    @Test
    void getUserByAccountId_UserNotFound() {
        // Mock repository to return empty
        UUID nonExistentAccountId = UUID.randomUUID();
        when(userRepository.findByAccountId(nonExistentAccountId)).thenReturn(Optional.empty());

        // Attempt to get user profile and expect exception
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.findUserByAccountId(nonExistentAccountId));
        assertTrue(exception.getMessage().contains("User not found"));
    }

    /**
     * Tests user profile update by account ID.
     */
    @Test
    void updateUserByAccountId_Success() {
        // Mock dependencies for successful update
        when(userRepository.findByAccountId(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toUserProfileDTO(any(User.class))).thenReturn(testUserDTO);

        // Update user profile by account ID
        UserProfileDTO result = userService.updateUserByAccountId(testUser.getAccountId(), updateDTO);

        // Verify user profile was updated successfully
        assertNotNull(result);
        verify(userRepository).findByAccountId(testUser.getAccountId());
        verify(userMapper).updateUserFromDTO(updateDTO, testUser);
        verify(userRepository).save(testUser);
    }

    /**
     * Tests user profile update by account ID with non-existing account.
     */
    @Test
    void updateUserByAccountId_UserNotFound() {
        // Mock repository to return empty
        UUID nonExistentAccountId = UUID.randomUUID();
        when(userRepository.findByAccountId(nonExistentAccountId)).thenReturn(Optional.empty());

        // Attempt to update user profile and expect exception
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.updateUserByAccountId(nonExistentAccountId, updateDTO));
        assertTrue(exception.getMessage().contains("User not found"));
    }

    /**
     * Tests email update from Auth Service.
     */
    @Test
    void handleEmailUpdate_Success() {
        // Mock repository to return test user
        UUID accountId = testUser.getAccountId();
        String newEmail = "newemail@example.com";
        String eventId = UUID.randomUUID().toString();
        Instant occurredAt = Instant.now();

        AccountEmailUpdateEvent event = new AccountEmailUpdateEvent(eventId, occurredAt, accountId, newEmail);

        when(userRepository.findByAccountId(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Update email from Auth Service
        userService.mirrorEmailUpdate(event);

        // Verify email was updated
        verify(userRepository).findByAccountId(testUser.getAccountId());
        verify(userRepository).save(testUser);
        assertEquals(newEmail, testUser.getEmail());
    }

    /**
     * Tests email update with non-existing account.
     */
    @Test
    void handleEmailUpdate_UserNotFound() {
        // Mock repository to return test user
        UUID nonExistentAccountId = UUID.randomUUID();
        String newEmail = "newemail@example.com";
        String eventId = UUID.randomUUID().toString();
        Instant occurredAt = Instant.now();

        AccountEmailUpdateEvent event = new AccountEmailUpdateEvent(eventId, occurredAt, nonExistentAccountId, newEmail);
        
        when(userRepository.findByAccountId(nonExistentAccountId)).thenReturn(Optional.empty());

        // Attempt to update email and expect exception
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.mirrorEmailUpdate(event));
        assertTrue(exception.getMessage().contains("User not found"));
    }

    /**
     * Tests username update from Auth Service.
     */
    @Test
    void handleUsernameUpdate_Success() {
        // Mock repository to return test user
        UUID accountId = UUID.randomUUID();
        String newUsername = "newusername";
        String eventId = UUID.randomUUID().toString();
        Instant occurredAt = Instant.now();

        AccountUsernameUpdateEvent event = new AccountUsernameUpdateEvent(eventId, occurredAt, accountId, newUsername);

        when(userRepository.findByAccountId(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Update username from Auth Service
        userService.mirrorUsernameUpdate(event);

        // Verify username was updated
        verify(userRepository).findByAccountId(testUser.getAccountId());
        verify(userRepository).save(testUser);
        assertEquals(newUsername, testUser.getUsername());
    }

    /**
     * Tests username update with non-existing account.
     */
    @Test
    void handleUsernameUpdate_UserNotFound() {
        // Mock repository to return empty
        UUID nonExistentAccountId = UUID.randomUUID();
        String newUsername = "newusername";
        String eventId = UUID.randomUUID().toString();
        Instant occurredAt = Instant.now();

        AccountUsernameUpdateEvent event = new AccountUsernameUpdateEvent(eventId, occurredAt, nonExistentAccountId, newUsername);
        
        when(userRepository.findByAccountId(nonExistentAccountId)).thenReturn(Optional.empty());

        // Attempt to update username and expect exception
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.mirrorUsernameUpdate(event));
        assertTrue(exception.getMessage().contains("User not found"));
    }



    /**
     * Tests partial update with null values.
     */
    @Test
    void updateUserProfile_PartialUpdate() {
        UserUpdateRequestDTO partialUpdate = UserUpdateRequestDTO.builder()
                .firstName("Updated")
                .build();

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toUserProfileDTO(any(User.class))).thenReturn(testUserDTO);

        UserProfileDTO result = userService.updateUserById(testUser.getId(), partialUpdate);

        assertNotNull(result);
        verify(userMapper).updateUserFromDTO(partialUpdate, testUser);
    }



    /**
     * Tests updating user with same email.
     */
    @Test
    void updateUser_SameEmail() {
        // Mock repository to return test user
        UUID accountId = testUser.getAccountId();
        String sameEmail = testUser.getEmail();
        String eventId = UUID.randomUUID().toString();
        Instant occurredAt = Instant.now();

        AccountEmailUpdateEvent event = new AccountEmailUpdateEvent(eventId, occurredAt, accountId, sameEmail);

        when(userRepository.findByAccountId(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.mirrorEmailUpdate(event);

        verify(userRepository).save(testUser);
        assertEquals(sameEmail, testUser.getEmail());
    }
    
}