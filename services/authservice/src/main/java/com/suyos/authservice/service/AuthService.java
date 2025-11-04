package com.suyos.authservice.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.client.UserClient;
import com.suyos.authservice.dto.AccountInfoDTO;
import com.suyos.authservice.dto.AccountLoginDTO;
import com.suyos.authservice.dto.AccountRegistrationDTO;
import com.suyos.authservice.dto.AuthenticationResponseDTO;
import com.suyos.authservice.dto.TokenRequestDTO;
import com.suyos.authservice.dto.UserCreationRequestDTO;
import com.suyos.authservice.mapper.AccountMapper;
import com.suyos.authservice.model.Account;
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
     * Creates a new account.
     * 
     * @param accountUpsertDTO Account registration data
     * @return Created account information
     * @throws RuntimeException If email already exists
     */
    public AccountInfoDTO createAccount(AccountRegistrationDTO accountRegistrationDTO) {
        // Fetch if there is an existing account for the given email
        if (accountRepository.existsByEmail(accountRegistrationDTO.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Map the account from the account registration DTO
        Account account = accountMapper.toEntity(accountRegistrationDTO);
        
        // Set extra account fields
        account.setPassword(passwordEncoder.encode(accountRegistrationDTO.getPassword()));
        account.setAccountEnabled(true);
        account.setEmailVerified(false);
        account.setFailedLoginAttempts(0);
        
        // Persist the created account
        Account savedAccount = accountRepository.save(account);
        
        // Call the User service to create a new user
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

        // Map the account's info DTO from the created account
        AccountInfoDTO accountInfoDTO = accountMapper.toAccountInfoDTO(savedAccount);
    
        // Return the created account's info DTO
        return accountInfoDTO;
    }

    /**
     * Authenticates an account during login attempt and generates JWT token.
     * 
     * @param accountLoginDTO Login credentials
     * @return Authentication response with JWT token and account ID
     * @throws RuntimeException If authentication fails
     */
    public AuthenticationResponseDTO authenticateAccount(AccountLoginDTO accountLoginDTO) {
        // Fetch if there is an active account for the given email
        Account account = accountRepository.findActiveByEmail(accountLoginDTO.getEmail())
            .orElseThrow(() -> new RuntimeException("Account not found for email: " + accountLoginDTO.getEmail()));
        
        // Check if the account is not locked
        if (account.getAccountLocked() && account.getLockedUntil() != null 
            && account.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Account is locked. Try again later.");
        }
        
        // Check if the entered password matches the account's password
        if (!passwordEncoder.matches(accountLoginDTO.getPassword(), account.getPassword())) {
            loginAttemptService.recordFailedAttempt(account);
            throw new RuntimeException("Invalid email or password");
        }
        
        // If login is performed successfully, update login related fields
        account.setFailedLoginAttempts(0);
        account.setLastLoginAt(LocalDateTime.now());
        account.setAccountLocked(false);
        account.setLockedUntil(null);
        
        // Persist the updated account
        Account savedAccount = accountRepository.save(account);
        
        // Generate JWT token for the successful login
        AuthenticationResponseDTO tokens = tokenService.issueTokens(savedAccount);

        // Return the authentication response DTO
        return tokens;
    }

    /**
     * Deauthenticates an account during logout.
     * 
     * @param tokenRequestDTO Token request
     * @throws RuntimeException If token is invalid
     */
    public void deauthenticateAccount(TokenRequestDTO tokenRequestDTO) {
        // Revoke the refresh token
        tokenService.revokeToken(tokenRequestDTO.getRefreshToken());
    }

    /**
     * Creates a new account from Google OAuth2 provider information.
     * 
     * @param email Account email from Google
     * @param name User full name from Google
     * @param providerId Unique identifier from Google
     * @return Created account entity
     */
    private Account createGoogleOAuth2Account(String email, String name, String providerId) {
        //String[] nameParts = name != null ? name.split(" ", 2) : new String[]{"User", ""};
        //String firstName = nameParts[0];
        //String lastName = nameParts.length > 1 ? nameParts[1] : "";
        
        Account account = Account.builder()
                .email(email)
                .username(email)
                .password("") // No password for OAuth2 users
                .oauth2Provider("google")
                .oauth2ProviderId(providerId)
                .emailVerified(true) // Google emails are pre-verified
                .accountEnabled(true)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .build();
        
        // Persist the created account
        Account savedAccount = accountRepository.save(account);

        // Return the created account
        return savedAccount;
    }

    /**
     * Processes Google OAuth2 authentication and creates or updates account.
     *
     * <p>Handles Google OAuth2 account information. Creates new account if not exists,
     * or updates existing OAuth2 account. Generates JWT token for API access.</p>
     *
     * @param email Account email from Google
     * @param name User full name from Google
     * @param providerId Unique identifier from Google
     * @return Authentication response with JWT token and account ID
     */
    public AuthenticationResponseDTO processGoogleOAuth2Account(String email, String name, String providerId) {
        // Find user either by OAuth2 credentials or fallback by email
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
            // Create a new user if not found by either method
            .orElseGet(() -> createGoogleOAuth2Account(email, name, providerId));

        // Update last login time
        account.setLastLoginAt(LocalDateTime.now());
        Account savedAccount = accountRepository.save(account);

        //  Generate JWT token for the successful login (same as traditional authentication)
        AuthenticationResponseDTO tokens = tokenService.issueTokens(savedAccount);
        
        // Return the authentication response DTO
        return tokens;
    }

}