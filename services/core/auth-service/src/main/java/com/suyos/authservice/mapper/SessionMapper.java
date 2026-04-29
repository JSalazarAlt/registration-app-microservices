package com.suyos.authservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.suyos.authservice.dto.internal.SessionCreationRequest;
import com.suyos.authservice.dto.response.SessionResponse;
import com.suyos.authservice.model.Session;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SessionMapper {

    /**
     * Converts a {@link SessionCreationRequest} to a {@link Session} entity.
     * 
     * @param request Session creation data
     * @return Created session entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "lastIpAddress", source = "firstIpAddress")
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "terminationReason", ignore = true)
    @Mapping(target = "terminatedAt", ignore = true)
    @Mapping(target = "lastActivityAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Session createFromRequest(SessionCreationRequest request);

    /**
     * Converts a {@link Session} entity to a {@link SessionResponse}.
     *
     * @param session Session entity
     * @return Session response
     */
    SessionResponse toResponse(Session session);
    
}