package com.suyos.authservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import com.suyos.authservice.dto.AccountInfoDTO;
import com.suyos.authservice.dto.AccountRegistrationDTO;
import com.suyos.authservice.dto.AccountUpdateDTO;
import com.suyos.authservice.model.Account;

/**
 * MapStruct mapper interface for converting between {@link Account} entities and 
 * DTOs.
 * 
 * <p>This interface defines the mapping contract between the internal Account entity
 * and various user-related DTOs. MapStruct generates the implementation at compile 
 * time, providing type-safe and efficient object mapping without reflection.</p>
 * 
 * <p>The Spring component model integration allows this mapper to be injected as a 
 * Spring bean into other components.</p>
 * 
 * @author Joel Salazar
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

    /**
     * Converts a {@link AccountUpsertDTO} to a {@link Account} entity.
     * 
     * <p>Ignores fields that are managed internally or belong to other services, 
     * such as identifiers and audit fields.</p>
     *
     * @param accountUpsertDTO {@link AccountUpsertDTO} containing user information
     * @return {@link Account} entity populated with created fields
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "mustChangePassword", ignore = true)
    @Mapping(target = "passwordChangedAt", ignore = true)
    @Mapping(target = "accountEnabled", ignore = true)
    @Mapping(target = "accountLocked", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "oauth2Provider", ignore = true)
    @Mapping(target = "oauth2ProviderId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Account toEntity(AccountRegistrationDTO accountRegistrationDTO);

    /**
     * Updates an existing {@link Account} entity with fields from 
     * {@link AccountUpdateDTO}. Null values and sensitive fields are ignored.
     * 
     * @param accountUpdateDTO {@link AccountUpdateDTO} containing updated values
     * @param account Existing {@link Account} entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "mustChangePassword", ignore = true)
    @Mapping(target = "passwordChangedAt", ignore = true)
    @Mapping(target = "accountEnabled", ignore = true)
    @Mapping(target = "accountLocked", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "oauth2Provider", ignore = true)
    @Mapping(target = "oauth2ProviderId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Account updateAccountFromDTO(AccountUpdateDTO accountUpdateDTO, @MappingTarget Account account);

    /**
     * Converts a {@link Account} entity to a {@link AccountInfoDTO}.
     *
     * @param account {@link Account} entity to convert
     * @return {@link AccountInfoDTO} representing public profile data
     */
    AccountInfoDTO toAccountInfoDTO(Account account);
    
}
