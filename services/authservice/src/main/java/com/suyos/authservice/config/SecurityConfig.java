package com.suyos.authservice.config;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
 * <p>Configures security filter chains, CORS settings, authentication providers,
 * and password encoding for the application. Supports both traditional JWT 
 * authentication and Google OAuth2 authentication.</p>
 * 
 * <p>Uses OAuth2 Resource Server for JWT validation (industry standard)
 * and OAuth2 Client for Google authentication.</p>
 * 
 * @author Joel Salazar
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Configures the main security rules and authentication mechanisms 
     * for the application.
     *
     * <p><b>Behavior:</b></p>
     * <ul>
     *   <li>Disables CSRF protection for stateless API requests authenticated 
     *       via JWT.</li>
     *   <li>Enables CORS using the predefined configuration from 
     *       {@code corsConfigurationSource()}.</li>
     *   <li>Defines authorization rules: public and protected endpoints.</li>
     *   <li>Sets session management to <b>stateless</b>, ensuring no HTTP session 
     *       persistence for JWT-based authentication.</li>
     *   <li>Configures OAuth2 login flow, specifying authorization and 
     *       redirection endpoints, and a custom success handler.</li>
     *   <li>Configures OAuth2 Resource Server for JWT validation using
     *       industry-standard approach.</li>
     * </ul>
     *
     * <p><b>Purpose:</b></p>
     * <ul>
     *   <li>Enforces consistent security policies across the API while supporting 
     *       both JWT and OAuth2 authentication methods.</li>
     *   <li>Separates public and protected routes to control access and prevent 
     *       unauthorized use of sensitive endpoints.</li>
     *   <li>Ensures the API operates securely in a stateless manner, suitable for 
     *       modern REST and microservice architectures.</li>
     *   <li>Integrates essential security layers — CORS, headers, and 
     *       authentication — into a unified filter chain.</li>
     * </ul>
     *
     * <hr>
     * 
     * @param http the {@link HttpSecurity} to configure
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if a configuration error occurs
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .headers(headers -> {
                headers.frameOptions(frame -> frame.deny());
                headers.contentTypeOptions(contentType -> contentType.disable());
                headers.httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true));
            })
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login").permitAll()
                .requestMatchers("/api/v1/auth/oauth2/google").permitAll()
                .requestMatchers("/api/v1/auth/verify-email", "/api/v1/auth/resend-verification").permitAll()
                .requestMatchers("/api/v1/auth/forgot-password", "/api/v1/auth/reset-password").permitAll()
                .requestMatchers("/api/v1/auth/refresh").permitAll()
                .requestMatchers("/api/v1/auth/logout").authenticated()
                .requestMatchers("/oauth2/**").permitAll()
                .anyRequest().authenticated())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .baseUri("/oauth2/authorize"))
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/oauth2/callback/*"))
                .successHandler(oauth2SuccessHandler))
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                .bearerTokenResolver(bearerTokenResolver()))
            .build();
    }

    /**
     * Configures Cross-Origin Resource Sharing (CORS) settings.
     *
     * <p><b>Behavior:</b></p>
     * <ol>
     *   <li>Defines a {@link CorsConfiguration} that specifies which origins,
     *       HTTP methods, and headers are allowed to access the application's 
     *       resources.</li>
     *   <li>Registers this configuration with a {@link UrlBasedCorsConfigurationSource},
     *       applying it to all request paths (<code>/**</code>).</li>
     * </ol>
     *
     * <p><b>Purpose:</b></p>
     * <ul>
     *   <li>Allows controlled access from the frontend (e.g., <code>http://localhost:5173</code>)
     *       to backend APIs hosted on a different origin.</li>
     *   <li>Prevents unauthorized or malicious domains from reading or modifying
     *       sensitive resources via cross-origin requests.</li>
     * </ul>
     *
     * <hr>
     * 
     * @return the configured {@link CorsConfigurationSource}
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
     * Configures the authentication manager.
     *
     * <p><b>Behavior:</b></p>
     * <ol>
     *   <li>Exposes the {@link AuthenticationManager} as a Spring bean, allowing 
     *       other components to inject and use it for authentication operations.</li>
     *   <li>Obtains the default {@link AuthenticationManager} from the provided
     *       {@link AuthenticationConfiguration}, which builds it based on the 
     *       application's configured authentication providers (e.g., user details 
     *       service, password encoder).</li>
     * </ol>
     *
     * <p><b>Purpose:</b></p>
     * <ul>
     *   <li>Acts as the central entry point for verifying user credentials within 
     *       the application.</li>
     *   <li>Processes an {@link Authentication} request (e.g., {@link UsernamePasswordAuthenticationToken}),
     *       delegates verification to the configured user details service, and 
     *       returns an authenticated {@link Authentication} instance if credentials 
     *       are valid.</li>
     * </ul>
     *
     * <hr>
     * 
     * @param config the authentication configuration
     * @return the configured authentication manager
     * @throws Exception if the authentication manager cannot be created
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configures JWT decoder for OAuth2 Resource Server.
     * 
     * @return the JWT decoder
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKey);
        return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(keyBytes, "HmacSHA256")).build();
    }

    /**
     * Configures the password encoder.
     * 
     * @return the BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();
        resolver.setAllowUriQueryParameter(false);
        
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
                path.startsWith("/oauth2/")) {
                return null;
            }
            
            return resolver.resolve(request);
        };
    }
        
}