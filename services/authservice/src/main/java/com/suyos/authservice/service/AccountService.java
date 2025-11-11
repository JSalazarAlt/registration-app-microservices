package com.suyos.authservice.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.dto.request.AccountUpdateRequestDTO;
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

    /** Mapper for converting between account entities and DTOs */
    private final AccountMapper accountMapper;

    /** Repository for account data access operations */
    private final AccountRepository accountRepository;

    /** Service for token management */
    private final TokenService tokenService;

    /** Password encoder for secure password hashing */
    private final PasswordEncoder passwordEncoder;

    /**
     * Finds an active account by ID.
     * 
     * @param id ID of the account to search for
     * @return Active account's information
     * @throws RuntimeException If active account is not found
     */
    public AccountInfoDTO findAccountById(UUID id) {
        // Find if there is an active account for the ID
        Account account = accountRepository.findActiveById(id)
            .orElseThrow(() -> new RuntimeException("Account not found for ID: " + id));

        // Map account's information from active account
        AccountInfoDTO accountInfo = accountMapper.toAccountInfoDTO(account);

        // Return the active account's information
        return accountInfo;
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

        // Map account's information from active account
        AccountInfoDTO accountInfo = accountMapper.toAccountInfoDTO(account);

        // Return the active account's information
        return accountInfo;
    }

    /**
     * Finds an active account by username.
     * 
     * @param username Username to search for
     * @return Active account's information
     * @throws RuntimeException If active account is not found
     */
    public AccountInfoDTO findAccountByUsername(String username) {
        // Find if there is an active account for the username
        Account account = accountRepository.findActiveByUsername(username)
            .orElseThrow(() -> new RuntimeException("Account not found for username: " + username));;
        
        // Map account's information from active account
        AccountInfoDTO accountInfo = accountMapper.toAccountInfoDTO(account);
        
        // Return the active account's information
        return accountInfo;
    }

    /**
     * Updates an active account by ID.
     * 
     * @param id ID of the account to update
     * @param request Account's update data
     * @return Updated account's information
     * @throws RuntimeException If active account is not found
     */
    public AccountInfoDTO updateAccountById(UUID id, AccountUpdateRequestDTO request) {
        // Find if there is an active account for the ID
        Account account = accountRepository.findActiveById(id)
            .orElseThrow(() -> new RuntimeException("Active account not found for ID: " + id));

        // Handle password change separately (security-sensitive)
        boolean passwordChanged = false;
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            account.setPassword(passwordEncoder.encode(request.getPassword()));
            account.setLastPasswordChangedAt(LocalDateTime.now());
            passwordChanged = true;
        }

        // Update account fields from account's information using mapper
        accountMapper.updateAccountFromDTO(request, account);

        // Persist updated account
        Account updatedAccount = accountRepository.save(account);

        // Revoke all valid tokens if password changed (security measure)
        if (passwordChanged) {
            tokenService.revokeAllTokensByAccountId(id);
        }

        // Map account's information from updated account
        AccountInfoDTO accountInfo = accountMapper.toAccountInfoDTO(updatedAccount);

        // Return updated account's information
        return accountInfo;
    }

    /**
     * Deletes the logged-in account using its ID.
     * 
     * @param id ID of the account to delete
     * @return Deleted account's information
     * @throws RuntimeException If active account is not found
     */
    public AccountInfoDTO deleteAccountById(UUID id) {
        // Find if there is an active account for the logged-in account ID
        Account account = accountRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        // Soft delete account
        account.setDeleted(true);
        account.setDeletedAt(LocalDateTime.now());

        // Persist updated account
        Account updatedAccount = accountRepository.save(account);

        // Map account's information from updated account
        AccountInfoDTO accountInfoDTO = accountMapper.toAccountInfoDTO(updatedAccount);

        // Return updated account's information DTO
        return accountInfoDTO;
    }
    
}