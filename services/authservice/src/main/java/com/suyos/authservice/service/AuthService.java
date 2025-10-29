package com.suyos.authservice.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.client.UserClient;
import com.suyos.authservice.dto.AccountInfoDTO;
import com.suyos.authservice.dto.AccountLoginDTO;
import com.suyos.authservice.dto.AccountUpsertDTO;
import com.suyos.authservice.dto.AuthenticationResponseDTO;
import com.suyos.authservice.dto.UserCreationRequestDTO;
import com.suyos.authservice.mapper.AccountMapper;
import com.suyos.authservice.model.Account;
import com.suyos.authservice.repository.AccountRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

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

    private final UserClient userClient;

    /** Security audit service for logging security events */
    /*
    private final SecurityAuditService securityAuditService;
     */

    /**
     * Creates a new account.
     * 
     * @param registrationDTO the registration information
     * @return the created user's profile information
     * @throws RuntimeException if email already exists
     */
    public AccountInfoDTO createAccount(AccountUpsertDTO accountUpsertDTO, 
        HttpServletRequest request) {
        // Fetch if there is an existing account for the given email
        if (accountRepository.existsByEmail(accountUpsertDTO.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        
        // Map specific fields for the new account
        Account account = accountMapper.toEntity(accountUpsertDTO);
        
        // Set extra account fields
        account.setPassword(passwordEncoder.encode(accountUpsertDTO.getPassword()));
        account.setAccountEnabled(true);
        account.setEmailVerified(false);
        account.setFailedLoginAttempts(0);
        
        // Persist the created account
        Account savedAccount = accountRepository.save(account);
        
        // Create user in UserService
        UserCreationRequestDTO userReq = new UserCreationRequestDTO();
        userReq.setAccountId(savedAccount.getId());
        userReq.setUsername(savedAccount.getUsername());
        userReq.setEmail(savedAccount.getEmail());
        userReq.setFirstName(accountUpsertDTO.getFirstName());
        userReq.setLastName(accountUpsertDTO.getLastName());
        userReq.setPhone(accountUpsertDTO.getPhone());
        userReq.setProfilePictureUrl(accountUpsertDTO.getProfilePictureUrl());
        userReq.setLocale(accountUpsertDTO.getLocale());
        userReq.setTimezone(accountUpsertDTO.getTimezone());
        userClient.createUser(userReq).block();
        
        // Return the account's info DTO
        return accountMapper.toAccountInfoDTO(savedAccount);
    }

    /**
     * Authenticates a user login attempt and generates JWT token.
     * 
     * @param userLoginDTO the login credentials
     * @return authentication response with JWT token and user profile
     * @throws RuntimeException if authentication fails
     */
    public AuthenticationResponseDTO authenticateAccount(AccountLoginDTO accountLoginDTO,
        HttpServletRequest request) {
        // Fetch if there is an existing account for the given email
        Account account = accountRepository.findActiveAccountByEmail(accountLoginDTO.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found for email: " + accountLoginDTO.getEmail()));
        
        // Check if the account is not locked
        if (account.getAccountLocked() && account.getLockedUntil() != null 
            && account.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Account is locked. Try again later.");
        }
        
        // Check if the entered password matches the account's password
        if (!passwordEncoder.matches(accountLoginDTO.getPassword(), account.getPassword())) {
            loginAttemptService.recordFailedAttempt(account);
            /*
            securityAuditService.logLoginAttempt(
                userLoginDTO.getEmail(),
                securityAuditService.getClientIp(request),
                securityAuditService.getUserAgent(request),
                false
            );
             */
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
        
        // Log successful login
        /*
        securityAuditService.logLoginAttempt(
            user.getEmail(),
            securityAuditService.getClientIp(request),
            securityAuditService.getUserAgent(request),
            true
        );
        */
        
        // Return the accounts's authentication response DTO
        return tokens;
    }

    /**
     * Processes Google OAuth2 authentication and creates or updates user account.
     *
     * Handles Google OAuth2 user information. Creates new user if not exists,
     * or updates existing OAuth2 user. Generates JWT token for API access.
     *
     * @param email user's email from Google
     * @param name user's full name from Google
     * @param providerId unique identifier from Google
     * @return authentication response with JWT token and user profile
     */
    public AuthenticationResponseDTO processGoogleOAuth2Account(String email, String name, String providerId) {
        // Find user either by OAuth2 credentials or fallback by email
        Account account = accountRepository.findByOauth2ProviderAndOauth2ProviderId("google", providerId)
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
        
        // Return the accounts's authentication response DTO
        return tokens;
    }

    /**
     * Creates a new user from Google OAuth2 provider information.
     * 
     * @param email user's email from Google
     * @param name user's full name from Google
     * @param providerId unique identifier from Google
     * @return newly created user entity
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

}