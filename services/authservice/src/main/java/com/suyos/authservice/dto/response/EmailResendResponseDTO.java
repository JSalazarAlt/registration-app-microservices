package com.suyos.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class EmailResendResponseDTO {

    /** Message after request */
    @Builder.Default
    private String message = "If your email is registered, a verification link has been sent.";
    
}
