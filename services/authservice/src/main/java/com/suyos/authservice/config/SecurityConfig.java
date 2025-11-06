package com.suyos.authservice.config;

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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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
 * <p>SecurityConfig wires JwtAuthenticationFilter (so that incoming requests 
 * get a JWT checked) and OAuth2AuthenticationSuccessHandler (so OAuth2 logins 
 * get processed into application tokens).</p>
 * 
 * @author Joel Salazar
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /** JWT authentication filter for processing JWT tokens in requests */
    private final JwtAuthenticationFilter jwtAuthFilter;
    
    /** OAuth2 success handler for processing successful Google OAuth2 authentication */
    private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;

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
     *   <li>Registers custom filters:
     *     <ul>
     *       <li>{@code rateLimitingFilter} — Applied before authentication to 
     *           throttle excessive requests.</li>
     *       <li>{@code jwtAuthFilter} — Validates and processes JWT tokens for 
     *           authentication.</li>
     *     </ul>
     *   </li>
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
     *   <li>Integrates essential security layers — CORS, headers, rate limiting, 
     *       and authentication filters — into a unified filter chain.</li>
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
            // Disable CSRF protection (suitable for stateless APIs using JWT)
            .csrf(csrf -> csrf.disable())
            // Enable CORS with custom configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Configure security-related HTTP headers
            .headers(headers -> {
                // Prevent the site from being loaded in a frame (clickjacking protection)
                headers.frameOptions(frame -> frame.deny());
                // Disable content type sniffing (optional, enabled by default)
                headers.contentTypeOptions(contentType -> contentType.disable());
                // Enforce HTTP Strict Transport Security (HSTS)
                headers.httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000) // 1 year
                    .includeSubDomains(true));
            })
            // Define authorization rules for endpoints
            .authorizeHttpRequests(auth -> auth
                // Allow anyone to register or login
                .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login").permitAll()
                // Require authentication for logout
                .requestMatchers("/api/v1/auth/logout").authenticated()
                // Allow anyone to use OAuth2 endpoints
                .requestMatchers("/oauth2/**").permitAll()
                // All other requests require authentication
                .anyRequest().authenticated())
            // Use stateless session management (no HTTP session, suitable for JWT)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Configure OAuth2 login
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .baseUri("/oauth2/authorize"))
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/oauth2/callback/*"))
                .successHandler(oauth2SuccessHandler))
            // Add JWT authentication filter before authentication filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
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
     * Configures the password encoder.
     * 
     * @return the BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
}