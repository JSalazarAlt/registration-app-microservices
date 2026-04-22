package com.suyos.authservice.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suyos.authservice.dto.request.AccountUpdateRequest;
import com.suyos.authservice.dto.response.AccountResponse;
import com.suyos.authservice.event.AccountEventProducer;
import com.suyos.authservice.exception.exceptions.AccountNotFoundException;
import com.suyos.authservice.exception.exceptions.EmailAlreadyRegisteredException;
import com.suyos.authservice.exception.exceptions.UsernameAlreadyTakenException;
import com.suyos.authservice.mapper.AccountMapper;
import com.suyos.authservice.model.Account;
import com.suyos.authservice.model.TokenType;
import com.suyos.authservice.repository.AccountRepository;
import com.suyos.authservice.specification.AccountSpecification;
import com.suyos.common.dto.response.PagedResponseDTO;
import com.suyos.common.event.AccountEmailUpdateEvent;
import com.suyos.common.event.AccountUsernameUpdateEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import com.suyos.authservice.model.AccountStatus;

/**
 * Service for account management operations.
 * 
 * <p>Handles account retrieval, update, and deletion operations. Provides
 * methods for locating accounts (e.g., by email or username) and supports 
 * soft deletion for audit and recovery purposes.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
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
    // RETRIEVAL
    // ----------------------------------------------------------------

    /**
     * Retrieves a paginated response of all accounts' information.
     * 
     * @param page Zero-based page index
     * @param size Page size
     * @param sortBy Field to sort by
     * @param sortDir Sort direction (asc/desc)
     * @param searchText Text to filter
     * @return Paginated response of all accounts' information
     */
    public PagedResponseDTO<AccountResponse> getAllAccounts(
        int page,
        int size,
        String sortBy,
        String sortDir,
        String searchText
    ) {
        // Define dynamic sorting rules
        Sort sort = Sort.by(sortBy);
        if ("desc".equalsIgnoreCase(sortDir)) {
            sort = sort.descending();
        }

        // Create search specification
        Specification<Account> spec = AccountSpecification.filter(
            searchText
        );

        // Create pageable request with dynamic sorting
        Pageable pageable = PageRequest.of(page, size, sort);

        // Retrieve all accounts
        Page<Account> accountPage = accountRepository.findAll(spec, pageable);
        
        // Map accounts' information from accounts
        List<AccountResponse> accountInfos = accountPage.getContent()
                .stream()
                .map(accountMapper::toResponse)
                .toList();

        // Build paginated response of all accounts' information
        PagedResponseDTO<AccountResponse> response = PagedResponseDTO.<AccountResponse>builder()
                .content(accountInfos)
                .currentPage(accountPage.getNumber())
                .totalPages(accountPage.getTotalPages())
                .totalElements(accountPage.getTotalElements())
                .size(accountPage.getSize())
                .first(accountPage.isFirst())
                .last(accountPage.isLast())
                .build();
        
        // Return paginated response of all accounts' information
        return response;
    }

    /**
     * Retrieves an account by ID.
     * 
     * @param id Account ID to search for
     * @return Account's information
     * @throws AccountNotFoundException If account is not found
     */
    public AccountResponse getAccountById(UUID id) {
        // Look up account by ID
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("account_id=" + id));

        // Log account retrieval success
        log.info("event=account_retrieved_by_id account_id={}", account.getId());

        // Map account's information from account
        AccountResponse accountInfo = accountMapper.toResponse(account);

        // Return account's information
        return accountInfo;
    }

    /**
     * Retrieves an account by email.
     * 
     * @param email Email to search for
     * @return Account's information
     * @throws AccountNotFoundException If account is not found
     */
    public AccountResponse getAccountByEmail(String email) {
        // Look up account by email
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("email=" + email));
        
        // Log account retrieval success
        log.info("event=account_retrieved_by_email account_id={} email={}", account.getId(), account.getEmail());

        // Map account's information from account
        AccountResponse accountInfo = accountMapper.toResponse(account);

        // Return account's information
        return accountInfo;
    }

    /**
     * Retrieves an account by username.
     * 
     * @param username Username to search for
     * @return Account's information
     * @throws AccountNotFoundException If account is not found
     */
    public AccountResponse getAccountByUsername(String username) {
        // Look up account by username
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new AccountNotFoundException("username=" + username));
        
        // Log account retrieval success
        log.info("event=account_retrieved_by_username account_id={} username={}", account.getId(), account.getUsername());
        
        // Map account's information from account
        AccountResponse accountInfo = accountMapper.toResponse(account);
        
        // Return account's information
        return accountInfo;
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    /**
     * Updates an account by ID.
     * 
     * @param id Account ID to update
     * @param request Account's new username and/or new email
     * @return Updated account's information
     * @throws AccountNotFoundException If account is not found
     */
    public AccountResponse updateAccountById(UUID id, AccountUpdateRequest request) {
        // Log account update attempt
        log.info("event=account_update_attempt account_id={}", id);

        // Look up account by ID
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("account_id=" + id));
        
        // Update username if update request includes it
        if (request.getUsername() != null) {
            // Check if username is already taken
            if (accountRepository.existsByUsername(request.getUsername())) {
                throw new UsernameAlreadyTakenException(request.getUsername());
            }

            // Update username if not taken
            account.setUsername(request.getUsername());

            // Generate random UUID for username update event and timestamp
            String usernameUpdateEventId = UUID.randomUUID().toString();
            Instant usernameUpdateEventTimestamp = Instant.now();
            
            // Build account username update event
            AccountUsernameUpdateEvent usernameEvent = AccountUsernameUpdateEvent.builder()
                    .id(usernameUpdateEventId)
                    .occurredAt(usernameUpdateEventTimestamp)
                    .accountId(account.getId())
                    .newUsername(request.getUsername())
                    .build();
                
            // Publish account username update event
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

            // Generate random UUID for email update event and timestamp
            String emailUpdateEventId = UUID.randomUUID().toString();
            Instant emailUpdateEventTimestamp = Instant.now();
            
            // Build account email update event
            AccountEmailUpdateEvent emailEvent = AccountEmailUpdateEvent.builder()
                    .id(emailUpdateEventId)
                    .occurredAt(emailUpdateEventTimestamp)
                    .accountId(account.getId())
                    .newEmail(request.getEmail())
                    .build();

            // Publish account email update event
            accountEventProducer.publishAccountEmailUpdate(emailEvent);
        }

        // Persist updated account
        Account updatedAccount = accountRepository.save(account);

        // Log account update success
        log.info("event=account_updated account_id={}", updatedAccount.getId());

        // Map account's information from updated account
        AccountResponse updatedAccountInfo = accountMapper.toResponse(updatedAccount);

        // Return updated account's information
        return updatedAccountInfo;
    }

    // ----------------------------------------------------------------
    // SOFT DELETION
    // ----------------------------------------------------------------

    /**
     * Soft deletes an account by ID.
     * 
     * @param id Account ID to soft delete
     * @return Soft-deleted account's information
     * @throws AccountNotFoundException If account is not found
     */
    public AccountResponse softDeleteAccountById(UUID id) {
        // Log account soft deletion attempt
        log.info("event=account_soft_deletion_attempt account_id={}", id);

        // Look up account by ID
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("account_id=" + id));
        
        // Soft delete account
        account.setStatus(AccountStatus.SOFT_DELETED);
        account.setSoftDeletedAt(Instant.now());

        // Persist soft-deleted account
        Account softDeletedAccount = accountRepository.save(account);

        // Map account's information from soft-deleted account
        AccountResponse softDeletedAccountInfo = accountMapper.toResponse(softDeletedAccount);

        // Revoke all valid refresh tokens linked to account
        tokenService.revokeAllTokensByAccountIdAndType(softDeletedAccount.getId(), TokenType.REFRESH);
        
        // Log account soft deletion success
        log.info("event=account_soft_deleted account_id={}", softDeletedAccount.getId());

        // Return soft-deleted account's information
        return softDeletedAccountInfo;
    }

    // ----------------------------------------------------------------
    // LOCK AND UNLOCK
    // ----------------------------------------------------------------

    /**
     * Locks an account by ID.
     * 
     * @param id Account ID to lock
     * @return Account's information
     * @throws AccountNotFoundException If account is not found
     */
    public AccountResponse lockAccountById(UUID id) {
        // Look up account by ID
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("account_id=" + id));

        // Lock account
        account.setLocked(true);
        account.setLockedUntil(Instant.now().plusSeconds(LOCK_DURATION_HOURS * 3600));

        // Persist locked account
        Account lockedAccount = accountRepository.save(account);

        // Log account lock success
        log.info("event=account_locked account_id={}", lockedAccount.getId());

        // Map account's information from locked account
        AccountResponse lockedAccountInfo = accountMapper.toResponse(lockedAccount);

        // Return locked account's information
        return lockedAccountInfo;
    }

    /**
     * Unlocks an account by ID.
     * 
     * @param id Account's ID to unlock
     * @return Account's information
     * @throws AccountNotFoundException If account is not found
     */
    public AccountResponse unlockAccountById(UUID id) {
        // Look up account by ID
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("account_id=" + id));

        // Unlock account
        account.setLocked(false);
        account.setLockedUntil(null);

        // Persist unlocked account
        Account unlockedAccount = accountRepository.save(account);

        // Log account unlock success
        log.info("event=account_unlocked account_id={}", unlockedAccount.getId());

        // Map account's information from unlocked account
        AccountResponse unlockedAccountInfo = accountMapper.toResponse(unlockedAccount);

        // Return account's information
        return unlockedAccountInfo;
    }
    
    // ----------------------------------------------------------------
    // HELPERS
    // ----------------------------------------------------------------

    /**
     * Updates last logout timestamp for an account by ID.
     * 
     * @param id Account's ID to update
     * @throws AccountNotFoundException If account is not found
     */
    public void updateLastLogout(UUID accountId) {
        // Look up account by ID
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("account_id=" + accountId));
        
        // Update last logout timestamp
        account.setLastLogoutAt(Instant.now());

        // Persist updated account
        accountRepository.save(account);
    }

}