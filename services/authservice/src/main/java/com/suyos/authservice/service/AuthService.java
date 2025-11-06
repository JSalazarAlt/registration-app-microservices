package com.suyos.authservice.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.client.UserClient;
import com.suyos.authservice.dto.internal.UserCreationRequestDTO;
import com.suyos.authservice.dto.request.AccountLoginDTO;
import com.suyos.authservice.dto.request.AccountRegistrationDTO;
import com.suyos.authservice.dto.request.TokenRequestDTO;
import com.suyos.authservice.dto.response.AccountInfoDTO;
import com.suyos.authservice.dto.response.AuthenticationResponseDTO;
import com.suyos.authservice.mapper.AccountMapper;
import com.suyos.authservice.model.Account;
import com.suyos.authservice.model.Role;
import com.suyos.authservice.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service for authentication and account management operations.
 *
 * <p>Handles account creation, authentication, OAuth2 integration, and
 * interservice communication with User Service. Manages JWT token
 * generation and account security features.</p>
 *
 * @author Joel Salazar
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    
    /** Repository for account data access operations */
    private final AccountRepository accountRepository;
    
    /** Mapper for converting between entities and DTOs */
    private final AccountMapper accountMapper;

    /** Service for handling failed login attempts and account locking */
    private final LoginAttemptService loginAttemptService;
    
    /** Password encoder for secure password hashing */
    private final PasswordEncoder passwordEncoder;
    
    /** JWT service for token generation and validation */
    private final TokenService tokenService;

    /** Web client for user-related interservice calls */
    private final UserClient userClient;

    /**
     * Creates a new account with the provided registration data.
     * 
     * @param accountRegistrationDTO Account registration data
     * @return Created account information
     * @throws RuntimeException If email or username already exists
     */
    public AccountInfoDTO createAccount(AccountRegistrationDTO accountRegistrationDTO) {
        // Check if email is already registered
        if (accountRepository.existsByEmail(accountRegistrationDTO.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Check if username is already taken
        if (accountRepository.existsByUsername(accountRegistrationDTO.getUsername())) {
            throw new RuntimeException("Username already registered");
        }

        // Map DTO to entity
        Account account = accountMapper.toEntity(accountRegistrationDTO);
        
        // Set security and status fields
        account.setPassword(passwordEncoder.encode(accountRegistrationDTO.getPassword()));
        account.setRole(Role.USER);
        account.setEnabled(true);
        account.setEmailVerified(false);
        account.setFailedLoginAttempts(0);
        
        // Save account to database
        Account savedAccount = accountRepository.save(account);
        
        // Create corresponding user profile in User Service
        UserCreationRequestDTO userReq = new UserCreationRequestDTO();
        userReq.setAccountId(savedAccount.getId());
        userReq.setUsername(savedAccount.getUsername());
        userReq.setEmail(savedAccount.getEmail());
        userReq.setFirstName(accountRegistrationDTO.getFirstName());
        userReq.setLastName(accountRegistrationDTO.getLastName());
        userReq.setPhone(accountRegistrationDTO.getPhone());
        userReq.setProfilePictureUrl(accountRegistrationDTO.getProfilePictureUrl());
        userReq.setLocale(accountRegistrationDTO.getLocale());
        userReq.setTimezone(accountRegistrationDTO.getTimezone());
        userClient.createUser(userReq).block();

        // Map entity to response DTO
        return accountMapper.toAccountInfoDTO(savedAccount);
    }

    /**
     * Authenticates account credentials and generates JWT tokens.
     * 
     * @param accountLoginDTO Login credentials
     * @return Authentication response with JWT tokens
     * @throws RuntimeException If authentication fails
     */
    public AuthenticationResponseDTO authenticateAccount(AccountLoginDTO accountLoginDTO) {
        // Find active account by email
        Account account = accountRepository.findActiveByEmail(accountLoginDTO.getEmail())
            .orElseThrow(() -> new RuntimeException("Account not found for email: " + accountLoginDTO.getEmail()));
        
        // Verify account is not deleted
        if (account.getDeleted()) {
            throw new RuntimeException("Account is deleted. Please reactivate your account.");
        }

        // Check account lock status
        if (account.getLocked()) {
            if (account.getLockedUntil() != null && account.getLockedUntil().isBefore(LocalDateTime.now())) {
                // Auto-unlock expired locks
                account.setLocked(false);
                account.setLockedUntil(null);
                account.setFailedLoginAttempts(0);
                accountRepository.save(account);
            } else {
                Duration remaining = Duration.between(LocalDateTime.now(), account.getLockedUntil());
                throw new RuntimeException(String.format("Account locked. Try again in %d minutes", remaining.toMinutes()));
            }
        }
        
        // Verify password
        if (!passwordEncoder.matches(accountLoginDTO.getPassword(), account.getPassword())) {
            loginAttemptService.recordFailedAttempt(account);
            throw new RuntimeException("Invalid email or password");
        }
        
        // Reset security fields on successful login
        account.setFailedLoginAttempts(0);
        account.setLastLoginAt(LocalDateTime.now());
        account.setLocked(false);
        account.setLockedUntil(null);
        
        // Save updated account
        Account savedAccount = accountRepository.save(account);
        
        // Generate JWT tokens
        return tokenService.issueTokens(savedAccount);
    }

    /**
     * Deauthenticates account and revokes refresh token.
     * 
     * @param tokenRequestDTO Token request containing refresh token
     * @throws RuntimeException If token is invalid
     */
    public void deauthenticateAccount(TokenRequestDTO tokenRequestDTO) {
        // Get account from refresh token
        Account account = tokenService.getAccountFromRefreshToken(tokenRequestDTO.getRefreshToken());
        
        // Update last logout timestamp
        account.setLastLogoutAt(LocalDateTime.now());
        accountRepository.save(account);
        
        // Revoke the refresh token
        tokenService.revokeToken(tokenRequestDTO.getRefreshToken());
    }

    /**
     * Creates a new account from Google OAuth2 authentication.
     * 
     * @param email Account email from Google
     * @param name User full name from Google
     * @param providerId Unique identifier from Google
     * @return Created account entity
     */
    private Account createGoogleOAuth2Account(String email, String name, String providerId) {
        Account account = Account.builder()
                .email(email)
                .username(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.USER)
                .oauth2Provider("google")
                .oauth2ProviderId(providerId)
                .emailVerified(true)
                .enabled(true)
                .locked(false)
                .failedLoginAttempts(0)
                .build();
        
        return accountRepository.save(account);
    }

    /**
     * Processes Google OAuth2 authentication (create or link account).
     *
     * <p>Creates new account if not exists, or links existing account with
     * Google OAuth2. Generates JWT tokens for API access.</p>
     *
     * @param email Account email from Google
     * @param name User full name from Google
     * @param providerId Unique identifier from Google
     * @return Authentication response with JWT tokens
     */
    public AuthenticationResponseDTO processGoogleOAuth2Account(String email, String name, String providerId) {
        // Find account by OAuth2 credentials or email
        Account account = accountRepository.findActiveByOauth2ProviderAndOauth2ProviderId("google", providerId)
            .or(() -> accountRepository.findByEmail(email)
                .map(existingAccount -> {
                    // Link existing account with Google
                    existingAccount.setOauth2Provider("google");
                    existingAccount.setOauth2ProviderId(providerId);
                    existingAccount.setEmailVerified(true);
                    return accountRepository.save(existingAccount);
                })
            )
            // Create new account if not found
            .orElseGet(() -> createGoogleOAuth2Account(email, name, providerId));

        // Update last login time
        account.setLastLoginAt(LocalDateTime.now());
        Account savedAccount = accountRepository.save(account);

        // Generate JWT tokens
        return tokenService.issueTokens(savedAccount);
    }

}