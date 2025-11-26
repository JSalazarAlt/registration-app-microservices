package com.suyos.userservice.service;

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

import com.suyos.userservice.dto.request.UserUpdateRequestDTO;
import com.suyos.userservice.dto.response.UserProfileDTO;
import com.suyos.userservice.mapper.UserMapper;
import com.suyos.userservice.model.User;
import com.suyos.userservice.repository.UserRepository;

/**
 * Unit tests for UserService.
 *
 * <p>Tests user profile business logic using mocked dependencies
 * to verify service behavior.</p>
 *
 * @author Joel Salazar
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
    void getUserProfileById_Success() {
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
    void getUserProfileById_UserNotFound() {
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
    void updateUserProfileById_Success() {
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
    void getUserProfileByAccountId_Success() {
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
    void updateUserProfileById_UserNotFound() {
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
    void getUserProfileByAccountId_UserNotFound() {
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
    void updateUserProfileByAccountId_Success() {
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
    void updateUserProfileByAccountId_UserNotFound() {
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
    void handleEmailUpdateFromAuth_Success() {
        // Mock repository to return test user
        String newEmail = "newemail@example.com";
        when(userRepository.findByAccountId(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Update email from Auth Service
        userService.mirrorEmailUpdate(testUser.getAccountId(), newEmail);

        // Verify email was updated
        verify(userRepository).findByAccountId(testUser.getAccountId());
        verify(userRepository).save(testUser);
        assertEquals(newEmail, testUser.getEmail());
    }

    /**
     * Tests email update with non-existing account.
     */
    @Test
    void handleEmailUpdateFromAuth_UserNotFound() {
        // Mock repository to return empty
        UUID nonExistentAccountId = UUID.randomUUID();
        when(userRepository.findByAccountId(nonExistentAccountId)).thenReturn(Optional.empty());

        // Attempt to update email and expect exception
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.mirrorEmailUpdate(nonExistentAccountId, "new@example.com"));
        assertTrue(exception.getMessage().contains("User not found"));
    }

    /**
     * Tests username update from Auth Service.
     */
    @Test
    void handleUsernameUpdateFromAuth_Success() {
        // Mock repository to return test user
        String newUsername = "newusername";
        when(userRepository.findByAccountId(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Update username from Auth Service
        userService.mirrorUsernameUpdate(testUser.getAccountId(), newUsername);

        // Verify username was updated
        verify(userRepository).findByAccountId(testUser.getAccountId());
        verify(userRepository).save(testUser);
        assertEquals(newUsername, testUser.getUsername());
    }

    /**
     * Tests username update with non-existing account.
     */
    @Test
    void handleUsernameUpdateFromAuth_UserNotFound() {
        // Mock repository to return empty
        UUID nonExistentAccountId = UUID.randomUUID();
        when(userRepository.findByAccountId(nonExistentAccountId)).thenReturn(Optional.empty());

        // Attempt to update username and expect exception
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.mirrorUsernameUpdate(nonExistentAccountId, "newusername"));
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
        String sameEmail = testUser.getEmail();
        when(userRepository.findByAccountId(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.mirrorEmailUpdate(testUser.getAccountId(), sameEmail);

        verify(userRepository).save(testUser);
        assertEquals(sameEmail, testUser.getEmail());
    }
    
}