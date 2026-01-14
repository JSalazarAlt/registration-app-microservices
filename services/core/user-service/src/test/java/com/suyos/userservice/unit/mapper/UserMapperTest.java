package com.suyos.userservice.unit.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.Test;
import org.mapstruct.factory.Mappers;

import com.suyos.common.event.UserCreationEvent;
import com.suyos.userservice.dto.request.UserUpdateRequest;
import com.suyos.userservice.dto.response.UserProfileResponse;
import com.suyos.userservice.mapper.UserMapper;
import com.suyos.userservice.model.User;

/**
 * Unit tests for {@link UserMapper}.
 *
 * <p>Tests MapStruct mapping logic between user entities and DTOs
 * to verify correct field transformations.</p>
 */
public class UserMapperTest {

    /** Mapper under test */
    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    /**
     * Maps user creation event to entity correctly.
     */
    @Test
    void toEntity_shouldMapCreationEventCorrectly() {
        // Build test user creation event
        UserCreationEvent event = UserCreationEvent.builder()
                .id(UUID.randomUUID().toString().toString())
                .occurredAt(Instant.now())
                .accountId(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phone("1234567890")
                .build();
        
        // Call user mapper to convert user creation event to user entity
        User user = userMapper.toEntity(event);

        // Assert mapped fields are correct
        assertThat(user)
                .isNotNull()
                .extracting(
                        User::getAccountId,
                        User::getUsername,
                        User::getEmail,
                        User::getFirstName,
                        User::getLastName,
                        User::getPhone
                )
                .containsExactly(
                        event.getAccountId(),
                        event.getUsername(),
                        event.getEmail(),
                        event.getFirstName(),
                        event.getLastName(),
                        event.getPhone()
                );
        
        // Assert unmapped fields are null
        assertThat(user.getId()).isNull();
        assertThat(user.getCreatedAt()).isNull();
        assertThat(user.getUpdatedAt()).isNull();
        assertThat(user.getTermsAcceptedAt()).isNull();
        assertThat(user.getPrivacyPolicyAcceptedAt()).isNull();
        assertThat(user.getDeleted()).isNull();
        assertThat(user.getDeletedAt()).isNull();
    }

    /**
     * Updates only allowed fields from DTO.
     */
    @Test
    void updateUserFromDTO_shouldUpdateOnlyAllowedFields() {
        // Build test user
        User testUser = User.builder()
                .id(UUID.randomUUID())
                .username("originalUser")
                .email("original@email.com")
                .firstName("Test")
                .lastName("User")
                .phone("111111")
                .accountId(UUID.randomUUID())
                .build();

        // Build test user's update request
        UserUpdateRequest request = UserUpdateRequest.builder()
                .firstName("Updated")
                .phone("0987654321")
                .build();
        
        // Call user mapper to update user from DTO
        userMapper.updateUserFromDTO(request, testUser);
        
        // Assert only allowed fields are updated
        assertThat(testUser)
                .extracting(User::getFirstName, User::getPhone)
                .containsExactly("Updated", "0987654321");
        
        // Assert other fields remain unchanged
        assertThat(testUser)
                .extracting(
                        User::getUsername,
                        User::getEmail,
                        User::getLastName
                )
                .containsExactly(
                        "originalUser",
                        "original@email.com",
                        "User"
                );
        
        // Assert ID and account ID are unchanged
        assertThat(testUser.getId()).isNotNull();
        assertThat(testUser.getAccountId()).isNotNull();
    }

    /**
     * Maps entity to profile response correctly.
     */
    @Test
    void toUserProfileDTO_shouldMapEntityToProfileResponse() {
        // Build test user
        User testUser = User.builder()
                .id(UUID.randomUUID())
                .username("originalUser")
                .email("original@email.com")
                .firstName("Test")
                .lastName("User")
                .phone("111111")
                .accountId(UUID.randomUUID())
                .build();

        // Call user mapper to convert user entity to profile response
        UserProfileResponse response = userMapper.toUserProfileDTO(testUser);

        // Assert mapped fields are correct
        assertThat(response)
                .isNotNull()
                .extracting(
                        UserProfileResponse::getUsername,
                        UserProfileResponse::getEmail,
                        UserProfileResponse::getFirstName,
                        UserProfileResponse::getLastName,
                        UserProfileResponse::getPhone
                )
                .containsExactly(
                        testUser.getUsername(),
                        testUser.getEmail(),
                        testUser.getFirstName(),
                        testUser.getLastName(),
                        testUser.getPhone()
                );
    }
        
}