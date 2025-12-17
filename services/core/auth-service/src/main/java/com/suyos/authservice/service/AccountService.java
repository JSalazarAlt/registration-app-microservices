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
import com.suyos.common.event.GlobalSessionTerminationEvent;
import com.suyos.common.model.SessionTerminationReason;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

/**
 * Service for account management operations.
 * 
 * <p>Handles account retrieval, update, and deletion operations. Provides
 * methods for locating accounts (e.g., by email or username) and supports 
 * soft-deletion for audit and recovery purposes.</p>
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
    // LOOKUP
    // ----------------------------------------------------------------

    /**
     * Finds a paginated list of all accounts.
     * 
     * @param page Zero-based page index
     * @param size Page size
     * @param sortBy Field to sort by
     * @param sortDir Sort direction (asc/desc)
     * @return Paginated list of accounts' information
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
        
        // Return paginated list of accounts' information
        return response;
    }

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
            .orElseThrow(() -> new AccountNotFoundException("account_id=" + id));

        // Log account found by ID success
        log.info("event=account_found_by_id account_id={}", account.getId());

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
            .orElseThrow(() -> new AccountNotFoundException("email=" + email));
        
        // Log account found by email success
        log.info("event=account_found_by_email account_id={}", account.getId());

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
            .orElseThrow(() -> new AccountNotFoundException("username=" + username));
        
        // Log account found by username success
        log.info("event=account_found_by_username account_id={}", account.getId());
        
        // Map account's information from account
        AccountInfoDTO accountInfo = accountMapper.toAccountInfoDTO(account);
        
        // Return account's information
        return accountInfo;
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    /**
     * Updates an account by ID.
     * 
     * @param id Account's ID to update
     * @param request Account's new username and/or new email
     * @return Updated account's information
     * @throws AccountNotFoundException If account is not found
     */
    public AccountInfoDTO updateAccountById(UUID id, AccountUpdateRequestDTO request) {
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
            
            // Build account's username update event
            AccountUsernameUpdateEvent usernameEvent = AccountUsernameUpdateEvent.builder()
                    .id(usernameUpdateEventId)
                    .occurredAt(usernameUpdateEventTimestamp)
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

            // Generate random UUID for email update event and timestamp
            String emailUpdateEventId = UUID.randomUUID().toString();
            Instant emailUpdateEventTimestamp = Instant.now();
            
            // Build account's email update event
            AccountEmailUpdateEvent emailEvent = AccountEmailUpdateEvent.builder()
                    .id(emailUpdateEventId)
                    .occurredAt(emailUpdateEventTimestamp)
                    .accountId(account.getId())
                    .newEmail(request.getEmail())
                    .build();

            // Publish account's email update event
            accountEventProducer.publishAccountEmailUpdate(emailEvent);
        }

        // Persist updated account
        Account updatedAccount = accountRepository.save(account);

        // Log account update success
        log.info("event=account_updated account_id={}", updatedAccount.getId());

        // Map account's information from updated account
        AccountInfoDTO accountInfo = accountMapper.toAccountInfoDTO(updatedAccount);

        // Return updated account's information
        return accountInfo;
    }

    // ----------------------------------------------------------------
    // SOFT-DELETION
    // ----------------------------------------------------------------

    /**
     * Soft-deletes an account by ID.
     * 
     * <p>Performs a soft-deletion by marking the account as deleted and
     * setting the deletion timestamp.</p>
     * 
     * @param id Account's ID to soft-delete
     * @return Soft-deleted account's information
     * @throws AccountNotFoundException If account is not found
     */
    public AccountInfoDTO softDeleteAccountById(UUID id) {
        // Log account soft-deletion attempt
        log.info("event=account_soft_deletion_attempt account_id={}", id);

        // Look up account by ID
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new AccountNotFoundException("account_id=" + id));
        
        // Soft-delete account
        account.setDeleted(true);
        account.setDeletedAt(Instant.now());

        // Persist soft-deleted account
        Account softDeletedAccount = accountRepository.save(account);

        // Map account's information from soft-deleted account
        AccountInfoDTO accountInfoDTO = accountMapper.toAccountInfoDTO(softDeletedAccount);

        // Revoke all valid refresh tokens linked to account
        tokenService.revokeAllTokensByAccountIdAndType(softDeletedAccount.getId(), TokenType.REFRESH);
        
        // Log account soft-deletion success
        log.info("event=account_soft_deleted account_id={}", softDeletedAccount.getId());

        // Generate random UUID and timestamp for session termination event
        String eventId = UUID.randomUUID().toString();
        Instant eventTimestamp = Instant.now();

        // Build global session termination event
        GlobalSessionTerminationEvent event = GlobalSessionTerminationEvent.builder()
                .id(eventId)
                .occurredAt(eventTimestamp)
                .accountId(softDeletedAccount.getId())
                .terminationReason(SessionTerminationReason.ACCOUNT_SOFT_DELETED)
                .build();
        
        // Publish global session termination event
        accountEventProducer.publishGlobalSessionTermination(event);

        // Return soft-deleted account's information
        return accountInfoDTO;
    }

    // ----------------------------------------------------------------
    // LOCK AND UNLOCK
    // ----------------------------------------------------------------

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
            .orElseThrow(() -> new AccountNotFoundException("account_id=" + id));

        // Lock account
        account.setLocked(true);
        account.setLockedUntil(Instant.now().plusSeconds(LOCK_DURATION_HOURS * 3600));

        // Persist updated account
        Account updatedAccount = accountRepository.save(account);

        // Log account lock success
        log.info("event=account_locked account_id={}", updatedAccount.getId());

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
            .orElseThrow(() -> new AccountNotFoundException("account_id=" + id));

        // Unlock account
        account.setLocked(false);
        account.setLockedUntil(null);

        // Persist updated account
        Account updatedAccount = accountRepository.save(account);

        // Log account unlock success
        log.info("event=account_unlocked account_id={}", updatedAccount.getId());

        // Map account's information from updated account
        AccountInfoDTO accountInfo = accountMapper.toAccountInfoDTO(updatedAccount);

        // Return account's information
        return accountInfo;
    }

}