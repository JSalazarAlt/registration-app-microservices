package com.suyos.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.public-key-location}")
    private Resource publicKeyResource;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(ex -> ex

                // Public endpoints from Auth microservice
                .pathMatchers(
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/oauth2/google",
                    "/api/auth/verify-email",
                    "/api/auth/resend-verification",
                    "/api/auth/forgot-password",
                    "/api/auth/reset-password",
                    "/api/auth/refresh"
                ).permitAll()

                // OAuth2 open endpoints
                .pathMatchers("/oauth2/**").permitAll()

                // Actuator public
                .pathMatchers("/actuator/**").permitAll()

                // Everything else requires JWT
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth ->
                oauth.jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
            );

        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        try {
            String pem = new String(publicKeyResource.getInputStream().readAllBytes())
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(pem);

            RSAPublicKey key = (RSAPublicKey) KeyFactory
                .getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));

            return NimbusReactiveJwtDecoder.withPublicKey(key).build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to load RSA public key", e);
        }
    }

}