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
     * @param id ID of the account to search for
     * @return Active account information
     * @throws RuntimeException If active account is not found
     */
    public AccountInfoDTO findAccountById(UUID id) {
        // Find if there is an active account for the ID
        Account account = accountRepository.findActiveById(id)
            .orElseThrow(() -> new RuntimeException("Account not found for ID: " + id));

        // Map the account's information DTO from the active account
        AccountInfoDTO accountInfoDTO = accountMapper.toAccountInfoDTO(account);

        // Return the active account's information DTO
        return accountInfoDTO;
    }

    /**
     * Finds an active account by email.
     * 
     * @param email Email to search for
     * @return Active account's information
     * @throws RuntimeException If active account is not found
     */
    public AccountInfoDTO findAccountByEmail(String email) {
        // Find if there is an active account for the email
        Account account = accountRepository.findActiveByEmail(email)
            .orElseThrow(() -> new RuntimeException("Account not found for email: " + email));

        // Map the account's information DTO from the active account
        AccountInfoDTO accountInfoDTO = accountMapper.toAccountInfoDTO(account);

        // Return the active account's information DTO
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
        // Find if there is an active account for the username
        Account account = accountRepository.findActiveByUsername(username)
            .orElseThrow(() -> new RuntimeException("Account not found for username: " + username));;
        
        // Map the account info DTO from the active account
        AccountInfoDTO accountInfoDTO = accountMapper.toAccountInfoDTO(account);
        
        // Return the active account's information DTO
        return accountInfoDTO;
    }

    /**
     * Finds the logged-in account using its ID.
     * 
     * @param authHeader Authentication header containing the access token
     * @return Logged-in account's information
     * @throws RuntimeException If active account is not found
     */
    public AccountInfoDTO findLoggedInAccount(String authHeader) {
        // Extract logged-in account ID from access token
        UUID loggedInAccountId = tokenService.extractAccountIdFromAccessToken(authHeader);

        // Find if there is an active account for the logged-in account ID
        Account account = accountRepository.findActiveById(loggedInAccountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Map account's information DTO from logged-in account
        AccountInfoDTO accountInfoDTO = accountMapper.toAccountInfoDTO(account);

        // Return logged-in account's information DTO
        return accountInfoDTO;
    }

    /**
     * Updates the logged-in account using its ID.
     * 
     * @param authHeader Authentication header containing the access token
     * @param accountUpdateDTO Account's update data
     * @return Updated account's information
     * @throws RuntimeException If active account is not found
     */
    public AccountInfoDTO updateLoggedInAccount(String authHeader, AccountUpdateDTO accountUpdateDTO) {
        // Extract logged-in account ID from access token
        UUID loggedInAccountId = tokenService.extractAccountIdFromAccessToken(authHeader);

        // Find if there is an active account for the logged-in account ID
        Account account = accountRepository.findActiveById(loggedInAccountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Handle password change separately (security-sensitive)
        boolean passwordChanged = false;
        if (accountUpdateDTO.getPassword() != null && !accountUpdateDTO.getPassword().isBlank()) {
            account.setPassword(passwordEncoder.encode(accountUpdateDTO.getPassword()));
            account.setLastPasswordChangedAt(LocalDateTime.now());
            passwordChanged = true;
        }

        // Update account fields from account's information DTO using mapper
        accountMapper.updateAccountFromDTO(accountUpdateDTO, account);

        // Persist updated account
        Account updatedAccount = accountRepository.save(account);

        // Revoke all tokens if password changed (security measure)
        if (passwordChanged) {
            tokenService.revokeAllTokensByAccountId(loggedInAccountId);
        }

        // Map account's information DTO from updated account
        AccountInfoDTO accountInfoDTO = accountMapper.toAccountInfoDTO(updatedAccount);

        // Return updated account's information DTO
        return accountInfoDTO;
    }

    /**
     * Deletes the logged-in account using its ID.
     * 
     * @param authHeader Authentication header containing the access token
     * @return Deleted account's information
     * @throws RuntimeException If active account is not found
     */
    public AccountInfoDTO deleteLoggedInAccount(String authHeader) {
        // Extract logged-in account ID from access token
        UUID loggedInAccountId = tokenService.extractAccountIdFromAccessToken(authHeader);

        // Find if there is an active account for the logged-in account ID
        Account account = accountRepository.findActiveById(loggedInAccountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        // Soft delete account
        account.setDeleted(true);
        account.setDeletedAt(LocalDateTime.now());

        // Persist updated account
        Account updatedAccount = accountRepository.save(account);

        // Map account's information DTO from updated account
        AccountInfoDTO accountInfoDTO = accountMapper.toAccountInfoDTO(updatedAccount);

        // Return updated account's information DTO
        return accountInfoDTO;
    }
    
}