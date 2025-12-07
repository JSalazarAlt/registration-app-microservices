package com.suyos.user.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.suyos.user.model.User;

/**
 * Repository for user entity data access operations.
 * 
 * <p>Provides standard CRUD operations for user entities and specific query
 * methods.</p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by account ID.
     *
     * @param accountId Account ID to search for
     * @return Optional containing user if found, empty otherwise
     */
    Optional<User> findByAccountId(UUID accountId);

    /**
     * Finds users by first or last name (case-insensitive partial match).
     *
     * @param firstName First name fragment to search for
     * @param lastName Last name fragment to search for
     * @return List of matching users
     */
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

}