package com.suyos.authservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.suyos.authservice.dto.internal.SessionCreationRequestDTO;
import com.suyos.authservice.dto.response.SessionInfoDTO;
import com.suyos.authservice.model.Session;

/**
 * Mapper for converting between {@link Session} entities and DTOs.
 * 
 * <p>Defines the mapping contract between the session entity and related
 * DTOs. MapStruct generates the implementation at compile time.</p>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SessionMapper {

    /**
     * Converts a {@link SessionDTO} to a {@link Session} entity.
     * 
     * @param request Account's registration data
     * @return Created account entity
     */
     
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "terminationReason", ignore = true)
    @Mapping(target = "terminatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Session toEntity(SessionCreationRequestDTO request);

    /**
     * Converts a {@link Session} entity to a {@link SessionInfoDTO}.
     *
     * @param session Session entity
     * @return Session's information
     */
    SessionInfoDTO toSessionInfoDTO(Session session);
    
}