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
    
    /** Mapper for converting between account entities and DTOs */
    private final AccountMapper accountMapper;

    /** Service for token generation and validation */
    private final TokenService tokenService;

    /** Service for handling failed login attempts and account locking */
    private final LoginAttemptService loginAttemptService;
    
    /** Password encoder for secure password hashing */
    private final PasswordEncoder passwordEncoder;

    /** Web client for user-related interservice calls */
    private final UserClient userClient;

    // TRADITIONAL LOGIN AND REGISTRATION

    /**
     * Creates a new account with the provided registration data.
     * 
     * @param accountRegistrationDTO Account's registration data
     * @return Created account's information DTO
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

        // Map account from account's registration DTO
        Account account = accountMapper.toEntity(accountRegistrationDTO);

        // Set password and default role
        account.setPassword(passwordEncoder.encode(accountRegistrationDTO.getPassword()));
        account.setRole(Role.USER);
        
        // Persist created account
        Account createdAccount = accountRepository.save(account);

        // Issue email verification token
        tokenService.issueEmailVerificationToken(createdAccount);
        
        // Call User service to create a new user
        UserCreationRequestDTO userReq = new UserCreationRequestDTO();
        userReq.setAccountId(createdAccount.getId());
        userReq.setUsername(createdAccount.getUsername());
        userReq.setEmail(createdAccount.getEmail());
        userReq.setFirstName(accountRegistrationDTO.getFirstName());
        userReq.setLastName(accountRegistrationDTO.getLastName());
        userReq.setPhone(accountRegistrationDTO.getPhone());
        userReq.setProfilePictureUrl(accountRegistrationDTO.getProfilePictureUrl());
        userReq.setLocale(accountRegistrationDTO.getLocale());
        userReq.setTimezone(accountRegistrationDTO.getTimezone());
        userClient.createUser(userReq).block();

        // Map account's information DTO from created account
        AccountInfoDTO accountInfoDTO = accountMapper.toAccountInfoDTO(createdAccount);
    
        // Return created account's information DTO
        return accountInfoDTO;
    }

    /**
     * Authenticates account credentials and generates tokens.
     * 
     * @param accountLoginDTO Login credentials
     * @return Authentication response DTO with refresh and access tokens
     * @throws RuntimeException If authentication fails
     */
    public AuthenticationResponseDTO authenticateAccount(AccountLoginDTO accountLoginDTO) {
        // Find if there is an active account for the email
        Account account = accountRepository.findActiveByEmail(accountLoginDTO.getEmail())
            .orElseThrow(() -> new RuntimeException("Account not found for email: " + accountLoginDTO.getEmail()));
        
        // Check if account is not deleted
        if (!account.getEmailVerified()) {
            throw new RuntimeException("Email is not verified. Please check your inbox for the verification email");
        }

        // Check if account is not deleted
        if (account.getDeleted()) {
            throw new RuntimeException("Account is deleted. Please reactivate your account if still possible");
        }

        // Check account's lock status
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
        
        // Persist updated account
        Account updatedAccount = accountRepository.save(account);
        
        // Generate refresh and access tokens for the successful login
        AuthenticationResponseDTO authenticationResponseDTO = tokenService.issueRefreshAndAccessTokens(updatedAccount);

        // Return authentication response DTO
        return authenticationResponseDTO;
    }

    /**
     * Deauthenticates account and revokes refresh token.
     * 
     * @param refreshTokenRequestDTO Refresh token request containing its value
     * @throws RuntimeException If token is invalid
     */
    public void deauthenticateAccount(TokenRequestDTO refreshTokenRequestDTO) {
        // Extract refresh token value from request
        String value = refreshTokenRequestDTO.getValue();

        // Find if there is an account for the refresh token
        Account account = tokenService.findAccountByToken(value);

        // Update last logout timestamp
        account.setLastLogoutAt(LocalDateTime.now());

        // Persist updated account
        accountRepository.save(account);
        
        // Revoke the refresh token
        tokenService.revokeToken(value);
    }

    // OAUTH2 LOGIN AND REGISTRATION

    /**
     * Creates a new account from Google OAuth2 authentication.
     * 
     * @param email Account email from Google
     * @param name User full name from Google
     * @param providerId Unique identifier from Google
     * @return Created account entity
     */
    private Account createGoogleOAuth2Account(String email, String name, String providerId) {
        // Create a new account with Google OAuth2 details
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

        // Persist the created account
        Account createdAccount = accountRepository.save(account);
        
        // Return the created account
        return createdAccount;
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
     * @return Authentication response DTO with refresh and access tokens
     */
    public AuthenticationResponseDTO processGoogleOAuth2Account(String email, String name, String providerId) {
        // Find if there is an active account for the OAuth2 credentials or email
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

        // Generate refresh and access tokens for the OAuth2 login
        AuthenticationResponseDTO authenticationResponseDTO = tokenService.issueRefreshAndAccessTokens(savedAccount);

        // Return authentication response DTO
        return authenticationResponseDTO;
    }

    // EMAIL MANAGEMENT

    /**
     * Verifies email using the email verification token.
     *
     * <p>Checks if the token is valid and associates it with the account.</p>
     *
     * @param emailVerificationTokenRequestDTO Email Verification token request 
     * containing its value
     * @return Authentication response DTO with refresh and access tokens
     */
    public AccountInfoDTO verifyEmail(TokenRequestDTO emailVerificationTokenRequestDTO) {
        // Extract email verification token value from request
        String value = emailVerificationTokenRequestDTO.getValue();

        // Check if token is valid
        if(tokenService.isTokenValid(value) == false) {
            throw new RuntimeException("Invalid token");
        }

        // Find account associated with the token
        Account account = tokenService.findAccountByToken(value);

        // Set email as verified
        account.setEmailVerified(true);

        // Persist updated account
        accountRepository.save(account);

        // Delete the used email verification token for cleanup
        tokenService.deleteToken(value);

        // Map account's information DTO from updated account
        AccountInfoDTO accountInfoDTO = accountMapper.toAccountInfoDTO(account);

        // Return account's information DTO
        return accountInfoDTO;
    }

}