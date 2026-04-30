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
import com.suyos.common.dto.response.PagedResponse;
import com.suyos.common.event.AccountEmailUpdateEvent;
import com.suyos.common.event.AccountUsernameUpdateEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import com.suyos.authservice.model.AccountStatus;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountService {

    private final AccountMapper accountMapper;

    private final AccountRepository accountRepository;

    private final AccountEventProducer accountEventProducer;

    private final TokenService tokenService;

    private static final int ACCOUNT_LOCK_DURATION_HOURS = 24;

    // ----------------------------------------------------------------
    // RETRIEVAL
    // ----------------------------------------------------------------

    /**
     * Gets a paginated response of all accounts, optionally filtered by
     * search text: username or email.
     * 
     * @param page Zero-based page index
     * @param size Page size
     * @param sortBy Field to sort by
     * @param sortDir Sort direction (asc/desc)
     * @param searchText Optional text to filter by
     * @return Paginated response of all accounts
     */
    public PagedResponse<AccountResponse> getAllAccounts(
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

        // Find all accounts filtered by search specification
        Page<Account> accountPage = accountRepository.findAll(spec, pageable);
        
        // Map account responses from accounts
        List<AccountResponse> accountResponses = accountPage.getContent()
                .stream()
                .map(accountMapper::toResponse)
                .toList();

        // Build paginated response of all accounts
        PagedResponse<AccountResponse> response = PagedResponse.<AccountResponse>builder()
                .content(accountResponses)
                .currentPage(accountPage.getNumber())
                .totalPages(accountPage.getTotalPages())
                .totalElements(accountPage.getTotalElements())
                .size(accountPage.getSize())
                .first(accountPage.isFirst())
                .last(accountPage.isLast())
                .build();
        
        // Log accounts retrieval success
        log.info("event=all_accounts_retrieved page={} size={} search_text={}", page, size, searchText);
        
        // Return paginated response of all accounts
        return response;
    }

    /**
     * Gets an account by its ID.
     * 
     * @param id ID of the account to retrieve
     * @return Account response
     * @throws AccountNotFoundException If account is not found
     */
    public AccountResponse getAccountById(UUID id) {
        // Find account by ID
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("account_id=" + id));

        // Log account retrieval success
        log.info("event=account_retrieved account_id={}", account.getId());

        // Map account response from account
        AccountResponse accountResponse = accountMapper.toResponse(account);

        // Return account response
        return accountResponse;
    }

    /**
     * Gets an account by its email.
     * 
     * @param email Email of the account to retrieve
     * @return Account response
     * @throws AccountNotFoundException If account is not found
     */
    public AccountResponse getAccountByEmail(String email) {
        // Find account by email
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("email=" + email));
        
        // Log account retrieval success
        log.info("event=account_retrieved account_id={} email={}", account.getId(), account.getEmail());

        // Map account response from account
        AccountResponse accountResponse = accountMapper.toResponse(account);

        // Return account response
        return accountResponse;
    }

    /**
     * Gets an account by its username.
     * 
     * @param username Username of the account to retrieve
     * @return Account response
     * @throws AccountNotFoundException If account is not found
     */
    public AccountResponse getAccountByUsername(String username) {
        // Find account by username
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new AccountNotFoundException("username=" + username));
        
        // Log account retrieval success
        log.info("event=account_retrieved account_id={} username={}", account.getId(), account.getUsername());
        
        // Map account response from account
        AccountResponse accountResponse = accountMapper.toResponse(account);
        
        // Return account response
        return accountResponse;
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    /**
     * Updates an account by its ID.
     * 
     * @param id ID of the account to update
     * @param request New username and/or email to update the account
     * @return Updated account response
     * @throws AccountNotFoundException If account is not found
     */
    public AccountResponse updateAccountById(UUID id, AccountUpdateRequest request) {
        // Log account update attempt
        log.info("event=account_update_attempt account_id={}", id);

        // Find account by ID
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("account_id=" + id));
        
        // Update username if update request includes it
        if (request.getUsername() != null && !request.getUsername().equals(account.getUsername())) {
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
        if (request.getEmail() != null && !request.getEmail().equals(account.getEmail())) {
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

        // Map account response from updated account
        AccountResponse accountResponse = accountMapper.toResponse(updatedAccount);

        // Return updated account response
        return accountResponse;
    }

    // ----------------------------------------------------------------
    // SOFT DELETION
    // ----------------------------------------------------------------

    /**
     * Soft deletes an account by its ID.
     * 
     * @param id ID of the account to soft delete
     * @return Soft-deleted account response
     * @throws AccountNotFoundException If account is not found
     */
    public AccountResponse softDeleteAccountById(UUID id) {
        // Log account soft deletion attempt
        log.info("event=account_soft_deletion_attempt account_id={}", id);

        // Find account by ID
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("account_id=" + id));
        
        // Soft delete account
        account.setStatus(AccountStatus.SOFT_DELETED);
        account.setSoftDeletedAt(Instant.now());

        // Persist soft-deleted account
        Account softDeletedAccount = accountRepository.save(account);

        // Map account response from soft-deleted account
        AccountResponse accountResponse = accountMapper.toResponse(softDeletedAccount);

        // Revoke all valid refresh tokens linked to account
        tokenService.revokeAllTokensByAccountIdAndType(softDeletedAccount.getId(), TokenType.REFRESH);
        
        // Log account soft deletion success
        log.info("event=account_soft_deleted account_id={}", softDeletedAccount.getId());

        // Return soft-deleted account response
        return accountResponse;
    }

    // ----------------------------------------------------------------
    // LOCK AND UNLOCK
    // ----------------------------------------------------------------

    /**
     * Locks an account by its ID.
     * 
     * @param id ID of the account to lock
     * @return Account response
     * @throws AccountNotFoundException If account is not found
     */
    public AccountResponse lockAccountById(UUID id) {
        // Find account by ID
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("account_id=" + id));

        // Lock account
        account.setLocked(true);
        account.setLockedUntil(Instant.now().plusSeconds(ACCOUNT_LOCK_DURATION_HOURS * 3600));

        // Persist locked account
        Account lockedAccount = accountRepository.save(account);

        // Log account lock success
        log.info("event=account_locked account_id={}", lockedAccount.getId());

        // Map account response from locked account
        AccountResponse accountResponse = accountMapper.toResponse(lockedAccount);

        // Return locked account response
        return accountResponse;
    }

    /**
     * Unlocks an account by its ID.
     * 
     * @param id ID of the account to unlock
     * @return Account response
     * @throws AccountNotFoundException If account is not found
     */
    public AccountResponse unlockAccountById(UUID id) {
        // Find account by ID
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("account_id=" + id));

        // Unlock account
        account.setLocked(false);
        account.setLockedUntil(null);

        // Persist unlocked account
        Account unlockedAccount = accountRepository.save(account);

        // Log account unlock success
        log.info("event=account_unlocked account_id={}", unlockedAccount.getId());

        // Map account response from unlocked account
        AccountResponse accountResponse = accountMapper.toResponse(unlockedAccount);

        // Return account response
        return accountResponse;
    }
    
    // ----------------------------------------------------------------
    // HELPERS
    // ----------------------------------------------------------------

    /**
     * Updates last logout timestamp for an account by ID.
     * 
     * @param id ID of the account to update
     * @throws AccountNotFoundException If account is not found
     */
    public void updateLastLogout(UUID accountId) {
        // Find account by ID
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("account_id=" + accountId));
        
        // Update last logout timestamp
        account.setLastLogoutAt(Instant.now());

        // Persist updated account
        accountRepository.save(account);
    }

}