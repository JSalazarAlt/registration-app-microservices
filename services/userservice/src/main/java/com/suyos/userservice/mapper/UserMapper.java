package com.suyos.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import com.suyos.userservice.dto.UserProfileDTO;
import com.suyos.userservice.dto.UserRegistrationDTO;
import com.suyos.userservice.dto.UserUpdateDTO;
import com.suyos.userservice.model.User;

/**
 * MapStruct mapper interface for converting between {@link User} entities and DTOs.
 * 
 * <p>This interface defines the mapping contract between the internal User entity
 * and various user-related DTOs. MapStruct generates the implementation at compile 
 * time, providing type-safe and efficient object mapping without reflection.</p>
 * 
 * <p>The Spring component model integration allows this mapper to be injected as a 
 * Spring bean into other components.</p>
 * 
 * @author Joel Salazar
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    /**
     * Converts a {@link UserRegistrationDTO} to a {@link User} entity.
     * 
     * <p>Ignores fields that are managed internally or belong to other services, 
     * such as identifiers and audit fields.</p>
     *
     * @param userRegistrationDTO {@link UserRegistrationDTO} containing user 
     * information
     * @return {@link User} entity populated with created fields
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "termsAcceptedAt", ignore = true)
    @Mapping(target = "privacyPolicyAcceptedAt", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserRegistrationDTO userRegistrationDTO);

    /**
     * Updates an existing {@link User} entity with fields from {@link UserUpdateDTO}.
     * Null values and sensitive fields are ignored.
     * 
     * @param userUpdateDTO {@link UserUpdateDTO} containing updated values
     * @param user Existing {@link User} entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "termsAcceptedAt", ignore = true)
    @Mapping(target = "privacyPolicyAcceptedAt", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateUserFromDTO(UserUpdateDTO userUpdateDTO, @MappingTarget User user);

    /**
     * Converts a {@link User} entity to a {@link UserProfileDTO}.
     *
     * @param user {@link User} entity to convert
     * @return {@link UserProfileDTO} representing public profile data
     */
    UserProfileDTO toProfileDTO(User user);

}