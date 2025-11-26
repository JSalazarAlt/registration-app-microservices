package com.suyos.authservice.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.dto.request.AccountUpdateRequestDTO;
import com.suyos.authservice.dto.response.AccountInfoDTO;
import com.suyos.authservice.event.AccountEventProducer;
import com.suyos.authservice.exception.exceptions.AccountNotFoundException;
import com.suyos.authservice.exception.exceptions.EmailAlreadyRegisteredException;
import com.suyos.authservice.exception.exceptions.UsernameAlreadyTakenException;
import com.suyos.authservice.mapper.AccountMapper;
import com.suyos.authservice.model.Account;
import com.suyos.authservice.model.TokenType;
import com.suyos.authservice.repository.AccountRepository;
import com.suyos.common.dto.response.PagedResponseDTO;
import com.suyos.common.event.AccountEmailUpdateEvent;
import com.suyos.common.event.AccountUsernameUpdateEvent;

import lombok.RequiredArgsConstructor;
import java.util.List;

/**
 * Service for account management operations.
 * 
 * <p>Handles account retrieval, update, and deletion operations. Provides
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

    /** Kafka producer for account events */
    private final AccountEventProducer accountEventProducer;

    /** Service for token management */
    private final TokenService tokenService;

    /** Account lock duration in hours */
    private static final int LOCK_DURATION_HOURS = 24;

    // ----------------------------------------------------------------
    // ADMIN
    // ----------------------------------------------------------------

    /**
     * Finds all accounts paginated.
     * 
     * @param page Page number to search for
     * @param size Size of page
     * @param sortBy Sort
     * @param sortDir Sort direction
     * @return All accounts' information
     */
    public PagedResponseDTO<AccountInfoDTO> findAllAccounts(int page, int size, 
        String sortBy, String sortDir) {
        // Define dynamic sorting rules
        Sort sort = Sort.by(sortBy);
        if ("desc".equalsIgnoreCase(sortDir)) {
            sort = sort.descending();
        }

        // Create pageable request with dynamic sorting
        Pageable pageable = PageRequest.of(page, size, sort);

        // Look up all accounts paginated
        Page<Account> accountPage = accountRepository.findAll(pageable);
        
        // Map accounts' information from accounts
        List<AccountInfoDTO> accountInfos = accountPage.getContent()
            .stream()
            .map(accountMapper::toAccountInfoDTO)
            .toList();

        // Build paginated response with all accounts' information
        PagedResponseDTO<AccountInfoDTO> response = PagedResponseDTO.<AccountInfoDTO>builder()
                .content(accountInfos)
                .currentPage(accountPage.getNumber())
                .totalPages(accountPage.getTotalPages())
                .totalElements(accountPage.getTotalElements())
                .size(accountPage.getSize())
                .first(accountPage.isFirst())
                .last(accountPage.isLast())
                .build();
        
        // Return all accounts' information paginated
        return response;
    }

    /**
     * Locks an account by ID.
     * 
     * @param id Account's ID to lock
     * @return Account's information
     * @throws AccountNotFoundException If account is not found
     */
    public AccountInfoDTO lockAccountById(UUID id) {
        // Look up account by ID
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new AccountNotFoundException(id.toString()));

        // Lock account
        account.setLocked(true);
        account.setLockedUntil(Instant.now().plusSeconds(LOCK_DURATION_HOURS * 3600));

        // Persist updated account
        Account updatedAccount = accountRepository.save(account);

        // Map account's information from updated account
        AccountInfoDTO accountInfo = accountMapper.toAccountInfoDTO(updatedAccount);

        // Return account's information
        return accountInfo;
    }

    /**
     * Unlocks an account by ID.
     * 
     * @param id Account's ID to unlock
     * @return Account's information
     * @throws AccountNotFoundException If account is not found
     */
    public AccountInfoDTO unlockAccountById(UUID id) {
        // Look up account by ID
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new AccountNotFoundException(id.toString()));

        // Unlock account
        account.setLocked(false);
        account.setLockedUntil(null);

        // Persist updated account
        Account updatedAccount = accountRepository.save(account);

        // Map account's information from updated account
        AccountInfoDTO accountInfo = accountMapper.toAccountInfoDTO(updatedAccount);

        // Return account's information
        return accountInfo;
    }

    // ----------------------------------------------------------------
    // ACCOUNT LOOKUP
    // ----------------------------------------------------------------

    /**
     * Finds an account by ID.
     * 
     * @param id Account's ID to search for
     * @return Account's information
     * @throws AccountNotFoundException If account is not found
     */
    public AccountInfoDTO findAccountById(UUID id) {
        // Look up account by ID
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new AccountNotFoundException(id.toString()));

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
     * @throws AccountNotFoundException If account is not found
     */
    public AccountInfoDTO findAccountByEmail(String email) {
        // Look up account by email
        Account account = accountRepository.findByEmail(email)
            .orElseThrow(() -> new AccountNotFoundException(email));

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
     * @throws AccountNotFoundException If account is not found
     */
    public AccountInfoDTO findAccountByUsername(String username) {
        // Look up account by username
        Account account = accountRepository.findByUsername(username)
            .orElseThrow(() -> new AccountNotFoundException(username));;
        
        // Map account's information from account
        AccountInfoDTO accountInfo = accountMapper.toAccountInfoDTO(account);
        
        // Return account's information
        return accountInfo;
    }

    // ----------------------------------------------------------------
    // ACCOUNT MANAGEMENT
    // ----------------------------------------------------------------

    /**
     * Updates an account by ID.
     * 
     * @param id Account's ID to update
     * @param request Account's update data
     * @return Updated account's information
     * @throws AccountNotFoundException If account is not found
     */
    public AccountInfoDTO updateAccountById(UUID id, AccountUpdateRequestDTO request) {
        // Look up account by ID
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new AccountNotFoundException(id.toString()));
        
        // Update username if update request includes it
        if (request.getUsername() != null) {
            // Check if username is already taken
            if (accountRepository.existsByUsername(request.getUsername())) {
                throw new UsernameAlreadyTakenException(request.getUsername());
            }

            // Update username if not taken
            account.setUsername(request.getUsername());
            
            // Build account's username update event
            AccountUsernameUpdateEvent usernameEvent = AccountUsernameUpdateEvent.builder()
                    .accountId(account.getId())
                    .newUsername(request.getUsername())
                    .build();
                
            // Publish account's username update event
            accountEventProducer.publishAccountUsernameUpdate(usernameEvent);
        }

        // Update email if update request includes it
        if (request.getEmail() != null) {
            // Check if email is already registered
            if (accountRepository.existsByEmail(request.getEmail())) {
                throw new EmailAlreadyRegisteredException(request.getEmail());
            }
            
            // Update email if not registered
            account.setEmail(request.getEmail());
            
            // Build account's email update event
            AccountEmailUpdateEvent emailEvent = AccountEmailUpdateEvent.builder()
                    .accountId(account.getId())
                    .newEmail(request.getEmail())
                    .build();

            // Publish account's email update event
            accountEventProducer.publishAccountEmailUpdate(emailEvent);
        }

        // Persist updated account
        Account updatedAccount = accountRepository.save(account);

        // Map account's information from updated account
        AccountInfoDTO accountInfo = accountMapper.toAccountInfoDTO(updatedAccount);

        // Return updated account's information
        return accountInfo;
    }

    /**
     * Soft deletes an account by ID.
     * 
     * <p>Performs a soft deletion by marking the account as deleted and
     * setting the deletion timestamp.</p>
     * 
     * @param id Account's ID to soft delete
     * @return Soft deleted account's information
     * @throws AccountNotFoundException If account is not found
     */
    public AccountInfoDTO softDeleteAccountById(UUID id) {
        // Look up account by ID
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new AccountNotFoundException(id.toString()));
        
        // Soft delete account
        account.setDeleted(true);
        account.setDeletedAt(Instant.now());

        // Update last logout timestamp
        account.setLastLogoutAt(Instant.now());

        // Persist soft deleted account
        Account softDeletedAccount = accountRepository.save(account);

        // Map account's information from soft deleted account
        AccountInfoDTO accountInfoDTO = accountMapper.toAccountInfoDTO(softDeletedAccount);

        // Revoke all valid refresh tokens linked to account
        tokenService.revokeAllTokensByAccountIdAndType(softDeletedAccount.getId(), TokenType.REFRESH);

        // Return soft deleted account's information
        return accountInfoDTO;
    }

    // ----------------------------------------------------------------
    // ACCOUNT CLEAN-UP
    // ----------------------------------------------------------------

}