package com.suyos.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for generic message responses.
 * 
 * <p>Used for simple API responses that only need to return a
 * message string, such as success confirmations or informational
 * messages.</p>
 * 
 * @author Joel Salazar
 */
@Getter
@AllArgsConstructor
@Builder
public class GenericMessageResponseDTO {

    /** Message after request */
    private String message;
    
}
