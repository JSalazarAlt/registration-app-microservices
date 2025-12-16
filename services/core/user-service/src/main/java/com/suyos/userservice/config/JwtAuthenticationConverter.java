package com.suyos.userservice.config;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Converts JWT tokens to Spring Security authentication objects.
 *
 * <p>Extracts account ID from JWT subject claim and role-based authorities
 * from JWT claims. Enables @AuthenticationPrincipal usage in controllers to
 * access authenticated account information.</p>
 */
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    /**
     * Converts JWT to authentication token.
     *
     * @param jwt JWT token to convert
     * @return Authentication token with authorities and account ID
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Extract authorities from JWT claims
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        
        // Create authentication token with JWT, authorities, and subject
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    /**
     * Extracts authorities from JWT claims.
     *
     * @param jwt JWT token
     * @return Collection of granted authorities
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        // Extract authorities list from JWT claims
        List<String> authorities = jwt.getClaimAsStringList("authorities");
        
        // Return empty list if no authorities found
        if (authorities == null) {
            return List.of();
        }

        // Convert string authorities to GrantedAuthority objects
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
    
}