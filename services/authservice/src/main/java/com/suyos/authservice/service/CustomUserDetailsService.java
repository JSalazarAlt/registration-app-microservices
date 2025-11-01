package com.suyos.authservice.service;

import java.util.ArrayList;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.suyos.authservice.model.Account;
import com.suyos.authservice.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

/**
 * Custom UserDetailsService for Spring Security authentication.
 *
 * <p>Loads user details from database for authentication and authorization.
 * Integrates with Spring Security's authentication mechanism.</p>
 *
 * @author Joel Salazar
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /** Repository for user data access */
    private final AccountRepository accountRepository;

    /**
     * Loads user details by email for authentication.
     * 
     * @param email User's email address
     * @return UserDetails object for Spring Security
     * @throws UsernameNotFoundException If user not found
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findActiveByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(account.getEmail())
                .password(account.getPassword())
                .authorities(new ArrayList<>())
                .accountExpired(false)
                .accountLocked(account.getAccountLocked())
                .credentialsExpired(false)
                .disabled(!account.getAccountEnabled())
                .build();
    }
    
}