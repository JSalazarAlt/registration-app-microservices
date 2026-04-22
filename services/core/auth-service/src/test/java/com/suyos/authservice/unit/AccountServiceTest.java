package com.suyos.authservice.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.suyos.authservice.dto.request.AccountUpdateRequest;
import com.suyos.authservice.dto.response.AccountResponse;
import com.suyos.authservice.event.AccountEventProducer;
import com.suyos.authservice.exception.exceptions.AccountNotFoundException;
import com.suyos.authservice.mapper.AccountMapper;
import com.suyos.authservice.model.Account;
import com.suyos.authservice.model.TokenType;
import com.suyos.authservice.repository.AccountRepository;
import com.suyos.authservice.service.AccountService;
import com.suyos.authservice.service.TokenService;
import com.suyos.common.dto.response.PagedResponseDTO;
import com.suyos.common.event.AccountUsernameUpdateEvent;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    /** Service under test */
    @InjectMocks
    private AccountService accountService;

    /** Mocked account mapper */
    @Mock
    private AccountMapper accountMapper;

    /** Mocked account repository */
    @Mock
    private AccountRepository accountRepository;

    /** Mocked account event producer */
    @Mock
    private AccountEventProducer accountEventProducer;

    /** Mocked token service */
    @Mock
    private TokenService tokenService;

    /** Account lock duration in hours */
    //private static final int LOCK_DURATION_HOURS = 24;

    /** Test account entity */
    private Account testAccount;

    /** Test account profile */
    private AccountResponse testAccountInfo;

    /** Test update request */
    private AccountUpdateRequest updateRequest;

	/** Updated test account profile */
    //private AccountResponse updatedTestAccountInfo;
    
    /**
     * Initializes common test data before each test.
     */
    @BeforeEach
    void setUp() {
        // Generate test account ID
        UUID accountId = UUID.randomUUID();

        // Build test account
        testAccount = Account.builder()
                .id(accountId)
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .build();

        // Build test account's profile
        testAccountInfo = AccountResponse.builder()
                .id(accountId)
                .username("testuser")
                .email("test@example.com")
                .build();

        // Build test account's update request
        updateRequest = AccountUpdateRequest.builder()
                .username("testuser1")
                .build();
    }

    // ----------------------------------------------------------
    // LOOKUP
    // ----------------------------------------------------------

    /**
     * Retrieves a paginated list of accounts successfully.
     */
    @Test
    void findAllAccounts_Success() {
		// Build paginated response with test account
        Page<Account> page = new PageImpl<>(
                List.of(testAccount),
                PageRequest.of(0, 10),
                1
        );

        // Mock account repository to return all test accounts when searching by pageable
        when(accountRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        // Mock account mapper to return test account profile when mapping test accounts
        when(accountMapper.toResponse(testAccount))
                .thenReturn(testAccountInfo);

        // Call service method to find all accounts with pagination
        PagedResponseDTO<AccountResponse> response = accountService.getAllAccounts(
                0,
                10,
                "username",
                "asc",
                "test"
        );

        // Assert expected accounts' information are returned
		assertThat(response).isNotNull();
		assertThat(response.getTotalElements())
				.isEqualTo(1);
		assertThat(response.getContent())
				.hasSize(1)
				.containsExactly(testAccountInfo);
    }

    /**
     * Retrieves an account's information by ID successfully.
     */
    @Test
    void findAccountById_Success() {
        // Mock account repository to return test account when searching by ID
        when(accountRepository.findById(testAccount.getId()))
                .thenReturn(Optional.of(testAccount));
        
        // Mock account mapper to return test account's information when mapping test account
        when(accountMapper.toResponse(testAccount))
                .thenReturn(testAccountInfo);

        // Call service method to find account by ID
        AccountResponse response = accountService.getAccountById(testAccount.getId());

        // Assert expected account's information is returned
        assertThat(response)
                .isNotNull()
                .isEqualTo(testAccountInfo);

        // Verify interactions
        verify(accountRepository).findById(testAccount.getId());
        verify(accountMapper).toResponse(testAccount);
    }

    /**
     * Throws exception when account is not found by ID.
     */
    @Test
    void findAccountById_AccountNotFound() {
        // Generate random user ID
        UUID id = UUID.randomUUID();

        // Mock account repository to return no account when searching random ID
        when(accountRepository.findById(id))
                .thenReturn(Optional.empty());

        // Assert expected exception is thrown
        assertThatThrownBy(() -> accountService.getAccountById(id))
                .isInstanceOf(AccountNotFoundException.class);

        // Verify interactions and no interactions
        verify(accountRepository).findById(id);
        verify(accountMapper, never()).toResponse(any());
    }

    // ----------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------

    /**
     * Updates an account by ID successfully.
     */
    @Test
    void updateAccountById_Success() {
        // Build test account's updated profile
        AccountResponse updatedTestAccountInfo = AccountResponse.builder()
                .id(testAccount.getId())
                .username("testuser1")
                .email("test@example.com")
                .build();

        // Mock account repository to return test account when searching by ID
        when(accountRepository.findById(testAccount.getId()))
                .thenReturn(Optional.of(testAccount));

        // Mock account repository to return no account when searching by new username
        // Test only username path since email is not updated in this test case
        when(accountRepository.existsByUsername(updateRequest.getUsername()))
                .thenReturn(false);

        // Mock account repository to return test account when saved
        when(accountRepository.save(testAccount))
                .thenReturn(testAccount);

        // Mock account mapper to return test account's information when mapping test account
        when(accountMapper.toResponse(testAccount))
                .thenReturn(updatedTestAccountInfo);
        
        // Call service method to update account by ID
        AccountResponse response = accountService.updateAccountById(testAccount.getId(), updateRequest);

        // Assert expected account's information is returned
        assertThat(response)
                .isNotNull()
                .isEqualTo(updatedTestAccountInfo);
        
        // Assert expected account's username is returned
        assertThat(testAccount.getUsername())
                .isEqualTo(updatedTestAccountInfo.getUsername());

        // Verify interactions
        verify(accountRepository).findById(testAccount.getId());
        verify(accountRepository).existsByUsername(updateRequest.getUsername());
        verify(accountRepository).save(testAccount);
        verify(accountEventProducer).publishAccountUsernameUpdate(any(AccountUsernameUpdateEvent.class));
        verify(accountEventProducer, never()).publishAccountEmailUpdate(any());
        verify(accountMapper).toResponse(testAccount);
    }

    // ----------------------------------------------------------------
    // SOFT DELETION
    // ----------------------------------------------------------------

    /**
     * Soft deletes an account by ID successfully.
     */
    @Test
    void softDeleteAccountById_Success() {
        // Mock account repository to return test account when searching by ID
        when(accountRepository.findById(testAccount.getId()))
                .thenReturn(Optional.of(testAccount));

        // Mock account repository to return test account when saved
        when(accountRepository.save(testAccount))
                .thenReturn(testAccount);

        // Mock account mapper to return test account's information when mapping test account
        when(accountMapper.toResponse(testAccount))
                .thenReturn(testAccountInfo);
        
        // Call service method to update account by ID
        AccountResponse response = accountService.softDeleteAccountById(testAccount.getId());

        // Assert expected account's information is returned
        assertThat(response)
                .isNotNull();

        // Assert deleted at field is set
        assertThat(testAccount.getSoftDeletedAt())
				.as("deletedAt should be set when user is soft-deleted")
				.isNotNull();

        // Verify interactions
        verify(accountRepository).findById(testAccount.getId());
        verify(accountRepository).save(testAccount);
        verify(accountMapper).toResponse(testAccount);
        verify(tokenService).revokeAllTokensByAccountIdAndType(
                testAccount.getId(),
                TokenType.REFRESH
        );
    }

    // ----------------------------------------------------------------
    // LOCK AND UNLOCK
    // ----------------------------------------------------------------

    /**
     * Locks an account by ID successfully.
     */
    @Test
    void lockAccountById_Success() {
        // Mock account repository to return test account when searching by ID
        when(accountRepository.findById(testAccount.getId()))
                .thenReturn(Optional.of(testAccount));

        // Mock account repository to return test account when saved
        when(accountRepository.save(testAccount))
                .thenReturn(testAccount);

        // Mock account mapper to return test account's information when mapping test account
        when(accountMapper.toResponse(testAccount))
                .thenReturn(testAccountInfo);
        
        // Call service method to update account by ID
        AccountResponse response = accountService.lockAccountById(testAccount.getId());

        // Assert expected account's information is returned
        assertThat(response)
                .isNotNull();

        // Assert account is locked
        assertThat(testAccount.getLocked())
				.as("locked should be set to true when account is locked")
				.isTrue();

        // Verify interactions
        verify(accountRepository).findById(testAccount.getId());
        verify(accountRepository).save(testAccount);
        verify(accountMapper).toResponse(testAccount);
    }

    /**
     * Unlocks an account by ID successfully.
     */
    @Test
    void unlockAccountById_Success() {
        // Mock account repository to return test account when searching by ID
        when(accountRepository.findById(testAccount.getId()))
                .thenReturn(Optional.of(testAccount));

        // Mock account repository to return test account when saved
        when(accountRepository.save(testAccount))
                .thenReturn(testAccount);

        // Mock account mapper to return test account's information when mapping test account
        when(accountMapper.toResponse(testAccount))
                .thenReturn(testAccountInfo);
        
        // Call service method to update account by ID
        AccountResponse response = accountService.unlockAccountById(testAccount.getId());

        // Assert expected account's information is returned
        assertThat(response)
                .isNotNull()
                .isEqualTo(testAccountInfo);

        // Assert account is unlocked
        assertThat(testAccount.getLocked())
				.as("locked should be set to false when account is unlocked")
				.isFalse();

        // Verify interactions
        verify(accountRepository).findById(testAccount.getId());
        verify(accountRepository).save(testAccount);
        verify(accountMapper).toResponse(testAccount);
    }

}