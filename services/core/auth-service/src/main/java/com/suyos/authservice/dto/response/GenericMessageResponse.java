package com.suyos.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data transfer object for generic message responses.
 * 
 * <p>Contains a simple message string used for confirmations or informational
 * responses.</p>
 */
@Getter
@AllArgsConstructor
@Builder
public class GenericMessageResponse {

    private final String message;
    
}