package com.suyos.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import com.suyos.common.event.UserCreationEvent;
import com.suyos.userservice.dto.request.UserUpdateRequestDTO;
import com.suyos.userservice.dto.response.UserProfileDTO;
import com.suyos.userservice.model.User;

/**
 * Mapper for converting between {@link User} entities and DTOs.
 * 
 * <p>Defines the mapping contract between the user entity and related
 * DTOs. MapStruct generates the implementation at compile time.</p>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    /**
     * Converts a {@link UserCreationEvent} to a {@link User} entity.
     *
     * @param event User's registration data
     * @return Created user entity
     */
    @Mapping(target = "id", ignore = true)
    //@Mapping(target = "username", ignore = true)
    //@Mapping(target = "email", ignore = true)
    @Mapping(target = "termsAcceptedAt", ignore = true)
    @Mapping(target = "privacyPolicyAcceptedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    //@Mapping(target = "accountId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserCreationEvent event);

    /**
     * Updates an existing {@link User} entity with fields from
     * {@link UserUpdateRequestDTO}.
     * 
     * @param userUpdateDTO User's update data
     * @param user Updated user entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "termsAcceptedAt", ignore = true)
    @Mapping(target = "privacyPolicyAcceptedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateUserFromDTO(UserUpdateRequestDTO userUpdateDTO, @MappingTarget User user);

    /**
     * Converts a {@link User} entity to a {@link UserProfileDTO}.
     *
     * @param user User entity
     * @return User's profile information
     */
    UserProfileDTO toUserProfileDTO(User user);

}