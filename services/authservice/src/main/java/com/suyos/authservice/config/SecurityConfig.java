package com.suyos.authservice.config;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security configuration for hybrid JWT and OAuth2 authentication.
 * 
 * <p>Configures security filter chains, CORS settings, authentication
 * providers, and password encoding. Supports both traditional JWT
 * authentication and Google OAuth2 authentication in a stateless
 * microservices architecture.</p>
 * 
 * <p>Uses OAuth2 Resource Server for industry-standard JWT validation
 * and OAuth2 Client for Google authentication. Implements custom
 * BearerTokenResolver to skip JWT validation on public endpoints.</p>
 * 
 * @author Joel Salazar
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    /**
     * Configures security filter chain with authentication mechanisms.
     *
     * <p><b>Behavior:</b></p>
     * <ul>
     *   <li>Disables CSRF for stateless JWT authentication</li>
     *   <li>Enables CORS for frontend communication</li>
     *   <li>Defines public and protected endpoints</li>
     *   <li>Sets stateless session management</li>
     *   <li>Configures OAuth2 login with custom success handler</li>
     *   <li>Configures OAuth2 Resource Server for JWT validation</li>
     * </ul>
     *
     * <p><b>Purpose:</b></p>
     * <ul>
     *   <li>Enforces security policies for JWT and OAuth2 methods</li>
     *   <li>Separates public and protected routes</li>
     *   <li>Ensures stateless operation for microservices</li>
     * </ul>
     * 
     * @param http HttpSecurity to configure
     * @return Configured security filter chain
     * @throws Exception If configuration error occurs
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            // Disable CSRF for stateless JWT authentication
            .csrf(csrf -> csrf.disable())
            // Enable CORS with configured source
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Configure security headers
            .headers(headers -> {
                headers.frameOptions(frame -> frame.deny());
                headers.contentTypeOptions(contentType -> contentType.disable());
                headers.httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true));
            })
            // Define authorization rules for endpoints
            .authorizeHttpRequests(auth -> auth
                // Public authentication endpoints
                .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login").permitAll()
                .requestMatchers("/api/v1/auth/oauth2/google").permitAll()
                // Email verification endpoints
                .requestMatchers("/api/v1/auth/verify-email", "/api/v1/auth/resend-verification").permitAll()
                // Password reset endpoints
                .requestMatchers("/api/v1/auth/forgot-password", "/api/v1/auth/reset-password").permitAll()
                // Refresh token endpoint
                .requestMatchers("/api/v1/auth/refresh").permitAll()
                // OAuth2 endpoints
                .requestMatchers("/oauth2/**").permitAll()
                // Prometheus and Grafana endpoints
                .requestMatchers("/actuator/prometheus").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                // Protected endpoints
                .requestMatchers("/api/v1/auth/logout").authenticated()
                .anyRequest().authenticated())
            // Set stateless session management
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Configure OAuth2 login with custom success handler
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .baseUri("/oauth2/authorize"))
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/oauth2/callback/*"))
                .successHandler(oauth2SuccessHandler))
            // Configure OAuth2 Resource Server for JWT validation
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                .bearerTokenResolver(bearerTokenResolver()))
            .build();
    }

    /**
     * Configures CORS settings for frontend communication.
     *
     * <p><b>Behavior:</b></p>
     * <ul>
     *   <li>Allows requests from frontend origin</li>
     *   <li>Permits all HTTP methods and headers</li>
     *   <li>Enables credentials for cookie-based auth</li>
     * </ul>
     *
     * <p><b>Purpose:</b></p>
     * <ul>
     *   <li>Enables frontend-backend communication</li>
     *   <li>Prevents unauthorized cross-origin access</li>
     * </ul>
     * 
     * @return Configured CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allows requests from the frontend
        configuration.addAllowedOrigin("http://localhost:5173");
        // Allows all HTTP methods
        configuration.addAllowedMethod("*");
        // Allows all headers
        configuration.addAllowedHeader("*");
        // Allows cookies and credentials
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configures JWT decoder using RSA public key (RS256).
     * 
     * @return JWT decoder with RS256 algorithm
     */
    @Bean
    public JwtDecoder jwtDecoder(@Value("${jwt.public-key}") Resource publicKeyResource) throws Exception {
        String key = new String(publicKeyResource.getInputStream().readAllBytes());

        key = key.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(decoded));

        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    /**
     * Configures password encoder for secure password hashing.
     * 
     * @return BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures custom bearer token resolver.
     * 
     * <p>Returns null for public endpoints to skip JWT validation,
     * allowing unauthenticated access to registration, login, and
     * password reset endpoints.</p>
     * 
     * @return Custom bearer token resolver
     */
    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        // Create default resolver
        DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();
        resolver.setAllowUriQueryParameter(false);
        
        // Return custom resolver that skips public endpoints
        return request -> {
            String path = request.getRequestURI();
            
            // Skip JWT validation for public endpoints
            if (path.equals("/api/v1/auth/register") || 
                path.equals("/api/v1/auth/login") ||
                path.equals("/api/v1/auth/oauth2/google") ||
                path.equals("/api/v1/auth/verify-email") ||
                path.equals("/api/v1/auth/resend-verification") ||
                path.equals("/api/v1/auth/forgot-password") ||
                path.equals("/api/v1/auth/reset-password") ||
                path.equals("/api/v1/auth/refresh") ||
                path.startsWith("/oauth2/") ||
                path.startsWith("/actuator")) {
                return null;
            }
            
            // Resolve bearer token for protected endpoints
            return resolver.resolve(request);
        };
    }
        
}