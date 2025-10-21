package com.suyos.userservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.suyos.userservice.model.User;

/**
 * Repository interface for User entity data access operations.
 * 
 * This interface extends JpaRepository to provide standard CRUD operations and 
 * authentication-specific query methods for User entities. Spring Data JPA
 * automatically generates the implementation at runtime.
 * 
 * Automatically available operations include:
 * - findAll() - retrieve all users
 * - findById() - find user by ID
 * - save() - create or update user
 * - deleteById() - delete user by ID
 * 
 * @author Joel Salazar
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Searches users by account ID.
     *
     * @param accountId Account ID to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByAccountId(UUID accountId);

    /**
     * Searches users by matching first or last name, ignoring case.
     *
     * @param firstName First name or part of it to search for
     * @param lastName Last name or part of it to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

}