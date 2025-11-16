package com.suyos.authservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import com.suyos.authservice.dto.request.RegistrationRequestDTO;
import com.suyos.authservice.dto.request.AccountUpdateRequestDTO;
import com.suyos.authservice.dto.response.AccountInfoDTO;
import com.suyos.authservice.model.Account;

/**
 * Mapper for converting between {@link Account} entities and DTOs.
 * 
 * <p>Defines the mapping contract between the account entity and related 
 * DTOs. MapStruct generates the implementation at compile time, providing
 * type-safe and efficient object mapping without reflection.</p>
 * 
 * @author Joel Salazar
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

    /**
     * Converts a {@link AccountUpsertDTO} to a {@link Account} entity.
     * 
     * <p>Ignores fields that are managed internally or belong to other
     * services, such as audit fields.</p>
     *
     * @param request Account's registration data
     * @return Account entity populated with created fields
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "locked", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "mustChangePassword", ignore = true)
    @Mapping(target = "lastPasswordChangedAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "lastLogoutAt", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "oauth2Provider", ignore = true)
    @Mapping(target = "oauth2ProviderId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "mfaEnabled", ignore = true)
    @Mapping(target = "mfaEnabledAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Account toEntity(RegistrationRequestDTO request);

    /**
     * Updates an existing {@link Account} entity with fields from
     * {@link AccountUpdateRequestDTO}.
     * 
     * <p>Ignores fields that are managed internally or belong to other
     * services, such as audit fields.</p>
     * 
     * @param request Account's update data
     * @param account Updated account entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "locked", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "mustChangePassword", ignore = true)
    @Mapping(target = "lastPasswordChangedAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "lastLogoutAt", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "oauth2Provider", ignore = true)
    @Mapping(target = "oauth2ProviderId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "mfaEnabled", ignore = true)
    @Mapping(target = "mfaEnabledAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Account updateAccountFromDTO(AccountUpdateRequestDTO request, @MappingTarget Account account);

    /**
     * Converts a {@link Account} entity to a {@link AccountInfoDTO}.
     *
     * @param account Account entity convert
     * @return Account's information representing profile data
     */
    AccountInfoDTO toAccountInfoDTO(Account account);
    
}