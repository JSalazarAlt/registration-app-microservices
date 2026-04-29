package com.suyos.authservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import com.suyos.authservice.dto.request.AccountUpdateRequest;
import com.suyos.authservice.dto.request.RegistrationRequest;
import com.suyos.authservice.dto.response.AccountResponse;
import com.suyos.authservice.model.Account;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

    /**
     * Creates an {@link Account} entity from a {@link RegistrationRequest}.
     * 
     * @param request Account registration data
     * @return Created account entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "locked", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "mustChangePassword", ignore = true)
    @Mapping(target = "lastPasswordChangedAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "lastLogoutAt", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "oauth2Provider", ignore = true)
    @Mapping(target = "oauth2ProviderId", ignore = true)
    @Mapping(target = "mfaEnabled", ignore = true)
    @Mapping(target = "mfaEnabledAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "softDeletedAt", ignore = true)
    @Mapping(target = "reactivatedAt", ignore = true)
    Account createFromRequest(RegistrationRequest request);

    /**
     * Updates an existing {@link Account} entity with fields from an
     * {@link AccountUpdateRequest}.
     * 
     * @param account Account entity to update
     * @param request Account update data
     * @return Updated account entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "locked", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "mustChangePassword", ignore = true)
    @Mapping(target = "lastPasswordChangedAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "lastLogoutAt", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "oauth2Provider", ignore = true)
    @Mapping(target = "oauth2ProviderId", ignore = true)
    @Mapping(target = "mfaEnabled", ignore = true)
    @Mapping(target = "mfaEnabledAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "softDeletedAt", ignore = true)
    @Mapping(target = "reactivatedAt", ignore = true)
    Account updateFromRequest(@MappingTarget Account account, AccountUpdateRequest request);

    /**
     * Converts an {@link Account} entity to a {@link AccountResponse}.
     *
     * @param account Account entity
     * @return Account response
     */
    AccountResponse toResponse(Account account);
    
}