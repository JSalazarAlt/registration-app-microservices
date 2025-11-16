package com.suyos.authservice.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.dto.request.AccountUpdateRequestDTO;
import com.suyos.authservice.dto.response.AccountInfoDTO;
import com.suyos.authservice.mapper.AccountMapper;
import com.suyos.authservice.model.Account;
import com.suyos.authservice.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for account management operations.
 * 
 * <p>Handles account retrieval, updates, and deletion operations. Provides
 * methods for locating accounts (e.g., by email or username) and supports 
 * soft deletion for audit and recovery purposes.</p>
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

    // ----------------------------------------------------------------
    // ADMIN
    // ----------------------------------------------------------------

    /**
     * Finds an account by ID.
     * 
     * @param id ID to search for
     * @return Account's information
     * @throws RuntimeException If account is not found
     */
    public List<AccountInfoDTO> findAllAccounts() {
        // Look up all accounts
        List<Account> accounts = accountRepository.findAll();

        // Map accounts' information from accounts
        List<AccountInfoDTO> accountInfos = accounts.stream()
            .map(accountMapper::toAccountInfoDTO)
            .collect(Collectors.toList());
        
        // Return all accounts' information
        return accountInfos;
    }

    // ----------------------------------------------------------------
    // ACCOUNT LOOKUP
    // ----------------------------------------------------------------

    /**
     * Finds an account by ID.
     * 
     * @param id ID to search for
     * @return Account's information
     * @throws RuntimeException If account is not found
     */
    public AccountInfoDTO findAccountById(UUID id) {
        // Look up account by ID
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Account not found for ID: " + id));

        // Map account's information from account
        AccountInfoDTO accountInfo = accountMapper.toAccountInfoDTO(account);

        // Return account's information
        return accountInfo;
    }

    /**
     * Finds an account by email.
     * 
     * @param email Email to search for
     * @return Account's information
     * @throws RuntimeException If account is not found
     */
    public AccountInfoDTO findAccountByEmail(String email) {
        // Look up account by email
        Account account = accountRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Account not found for email: " + email));

        // Map account's information from account
        AccountInfoDTO accountInfo = accountMapper.toAccountInfoDTO(account);

        // Return account's information
        return accountInfo;
    }

    /**
     * Finds an account by username.
     * 
     * @param username Username to search for
     * @return Account's information
     * @throws RuntimeException If account is not found
     */
    public AccountInfoDTO findAccountByUsername(String username) {
        // Look up account by username
        Account account = accountRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Account not found for username: " + username));;
        
        // Map account's information from account
        AccountInfoDTO accountInfo = accountMapper.toAccountInfoDTO(account);
        
        // Return account's information
        return accountInfo;
    }

    // ACCOUNT UPDATE

    /**
     * Updates an account by ID.
     * 
     * @param id Account ID to update
     * @param request Account's update data
     * @return Updated account's information
     * @throws RuntimeException If account is not found
     */
    public AccountInfoDTO updateAccountById(UUID id, AccountUpdateRequestDTO request) {
        // Look up account by ID
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Account not found for ID: " + id));

        // Update account fields using mapper
        accountMapper.updateAccountFromDTO(request, account);

        // Persist updated account
        Account updatedAccount = accountRepository.save(account);

        // Map account's information from updated account
        AccountInfoDTO accountInfo = accountMapper.toAccountInfoDTO(updatedAccount);

        // Return updated account's information
        return accountInfo;
    }

    // ACCOUNT SOFT DELETION

    /**
     * Soft deletes an account by ID.
     * 
     * <p>Performs a soft deletion by marking the account as deleted and
     * setting the deletion timestamp.</p>
     * 
     * @param id Account ID to soft delete
     * @return Soft deleted account's information
     * @throws RuntimeException If account is not found
     */
    public AccountInfoDTO softDeleteAccountById(UUID id) {
        // Look up account by ID
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Account not found for ID: " + id));
        
        // Soft delete account
        account.setDeleted(true);
        account.setDeletedAt(LocalDateTime.now());

        // Persist soft deleted account
        Account updatedAccount = accountRepository.save(account);

        // Map account's information from soft deleted account
        AccountInfoDTO accountInfoDTO = accountMapper.toAccountInfoDTO(updatedAccount);

        // Return soft deleted account's information
        return accountInfoDTO;
    }

    // ACCOUNT CLEAN-UP

}