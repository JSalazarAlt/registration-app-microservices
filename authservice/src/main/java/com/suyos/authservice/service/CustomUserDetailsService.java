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
 * Custom UserDetailsService implementation for Spring Security authentication.
 * 
 * Loads user details from the database for authentication and authorization.
 * Integrates with Spring Security's authentication mechanism.
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
     * @param email the user's email address
     * @return UserDetails object for Spring Security
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findActiveAccountByEmail(email)
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