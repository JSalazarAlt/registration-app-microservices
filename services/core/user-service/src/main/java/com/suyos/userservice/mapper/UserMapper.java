package com.suyos.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import com.suyos.common.event.UserCreationEvent;
import com.suyos.userservice.dto.request.UserUpdateRequest;
import com.suyos.userservice.dto.response.UserResponse;
import com.suyos.userservice.model.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    /**
     * Creates a {@link User} entity from a {@link UserCreationEvent}.
     * 
     * @param event User creation event
     * @return Created user entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "termsAcceptedAt", ignore = true)
    @Mapping(target = "privacyPolicyAcceptedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "softDeletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User createFromRequest(UserCreationEvent event);

    /**
     * Updates an existing {@link User} entity with fields from
     * {@link UserUpdateRequest}.
     * 
     * @param user Updated user entity
     * @param request User update data
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "termsAcceptedAt", ignore = true)
    @Mapping(target = "privacyPolicyAcceptedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "softDeletedAt", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromRequest(@MappingTarget User user, UserUpdateRequest request);

    /**
     * Converts a {@link User} entity to a {@link UserResponse}.
     *
     * @param user User entity
     * @return User response
     */
    UserResponse toResponse(User user);

}