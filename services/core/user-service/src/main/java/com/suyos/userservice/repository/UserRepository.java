package com.suyos.userservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.suyos.userservice.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    // ----------------------------------------------------------------
    // LOOKUP
    // ----------------------------------------------------------------

    Optional<User> findByAccountId(UUID accountId);

}