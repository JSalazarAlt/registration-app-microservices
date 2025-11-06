package com.suyos.authservice.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.dto.request.AccountUpdateDTO;
import com.suyos.authservice.dto.response.AccountInfoDTO;
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
public class AccountService {

    /** Repository for account data access operations */
    private final AccountRepository accountRepository;
    
    /** Mapper for converting between entities and DTOs */
    private final AccountMapper accountMapper;

    /** Password encoder for secure password hashing */
    private final PasswordEncoder passwordEncoder;

    /** JWT service for token generation and validation */
    private final TokenService tokenService;

    /**
     * Finds an active account by ID.
     * 
     * @param accountId ID of the account to search for
     * @return Active account information
     * @throws RuntimeException If active account is not found
     */
    public AccountInfoDTO findAccountById(UUID accountId) {
        // Fetch if there is an active account for the given ID
        Account account = accountRepository.findActiveById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found for ID: " + accountId));
        
        // Map the account info DTO from the active account
        AccountInfoDTO accountInfoDTO = accountMapper.toAccountInfoDTO(account);
        
        // Return the active account info DTO
        return accountInfoDTO;
    }

    /**
     * Finds an active account by email.
     * 
     * @param email Email to search for
     * @return Active account information
     * @throws RuntimeException If active account is not found
     */
    public AccountInfoDTO findAccountByEmail(String email) {
        // Fetch if there is an active account for the given email
        Account account = accountRepository.findActiveByEmail(email)
            .orElseThrow(() -> new RuntimeException("Account not found for email: " + email));
        
        // Map the account info DTO from the active account
        AccountInfoDTO accountInfoDTO = accountMapper.toAccountInfoDTO(account);
        
        // Return the active account info DTO
        return accountInfoDTO;
    }

    /**
     * Finds an active account by username.
     * 
     * @param username Username to search for
     * @return Active account information
     * @throws RuntimeException If active account is not found
     */
    public AccountInfoDTO findAccountByUsername(String username) {
        // Fetch if there is an active account for the given username
        Account account = accountRepository.findActiveByUsername(username)
            .orElseThrow(() -> new RuntimeException("Account not found for username: " + username));;
        
        // Map the account info DTO from the active account
        AccountInfoDTO accountInfoDTO = accountMapper.toAccountInfoDTO(account);
        
        // Return the active account info DTO
        return accountInfoDTO;
    }

    /**
     * Finds the logged-in account using its ID.
     * 
     * @param authHeader Authentication header containing the access token
     * @return Active account information
     * @throws RuntimeException If active account is not found
     */
    public AccountInfoDTO findLoggedInAccount(String authHeader) {
        // Extract account ID from the token
        UUID loggedInAccountId = tokenService.getAccountIdFromAccessToken(authHeader);

        // Fetch if there is an active account for the logged-in ID
        Account account = accountRepository.findActiveById(loggedInAccountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Map the account info DTO from the logged-in account
        AccountInfoDTO accountInfoDTO = accountMapper.toAccountInfoDTO(account);

        // Return the logged-in account info DTO
        return accountInfoDTO;
    }

    /**
     * Updates the logged-in account using its ID.
     * 
     * @param authHeader Authentication header containing the access token
     * @param accountUpdateDTO Account registration data
     * @return Updated account information
     * @throws RuntimeException If active account is not found
     */
    public AccountInfoDTO updateLoggedInAccount(String authHeader, AccountUpdateDTO accountUpdateDTO) {
        // Extract account ID from the token
        UUID loggedInAccountId = tokenService.getAccountIdFromAccessToken(authHeader);

        // Fetch if there is an active account for the logged-in ID
        Account account = accountRepository.findActiveById(loggedInAccountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Handle password change separately (security-sensitive)
        boolean passwordChanged = false;
        if (accountUpdateDTO.getPassword() != null && !accountUpdateDTO.getPassword().isBlank()) {
            account.setPassword(passwordEncoder.encode(accountUpdateDTO.getPassword()));
            account.setLastPasswordChangedAt(LocalDateTime.now());
            passwordChanged = true;
        }

        // Update other fields using mapper
        accountMapper.updateAccountFromDTO(accountUpdateDTO, account);

        // Persist the updated account
        Account savedAccount = accountRepository.save(account);

        // Revoke all tokens if password changed (security measure)
        if (passwordChanged) {
            tokenService.revokeAllTokensByAccountId(loggedInAccountId);
        }

        // Map the account info DTO from the logged-in account
        AccountInfoDTO accountInfoDTO = accountMapper.toAccountInfoDTO(savedAccount);

        // Return the updated logged-in account info DTO
        return accountInfoDTO;
    }

    /**
     * Deletes the logged-in account using its ID.
     * 
     * @param loggedInAccountId ID of the logged-in account
     * @return Deleted account information
     * @throws RuntimeException If active account is not found
     */
    public AccountInfoDTO deleteLoggedInAccount(String authHeader) {
        // Extract account ID from the token
        UUID loggedInAccountId = tokenService.getAccountIdFromAccessToken(authHeader);

        // Fetch if there is an active account for the logged-in ID
        Account account = accountRepository.findActiveById(loggedInAccountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        // Soft delete the account
        account.setDeleted(true);
        account.setDeletedAt(LocalDateTime.now());

        // Persist the updated account
        Account savedAccount = accountRepository.save(account);

        // Map the account info DTO from the logged-in account
        AccountInfoDTO accountInfoDTO = accountMapper.toAccountInfoDTO(savedAccount);

        // Return the updated logged-in account info DTO
        return accountInfoDTO;
    }
    
}
