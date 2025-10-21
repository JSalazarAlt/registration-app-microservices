package com.suyos.userservice.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a user in the registration and authentication system.
 * 
 * This class maps to the 'users' table in the database and contains
 * all the necessary fields for user account management and authentication.
 * 
 * @author Joel Salazar
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_username", columnList = "username")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /** Unique identifier for the user record */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    /** User's chosen username for display purposes */
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    
    /** User's first name for personal identification */
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /** User's last name for personal identification */
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /** User's email address used for login and communication */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /** User's phone number for contact purposes */
    @Column(name = "phone")
    private String phone;

    /** URL to the user's profile picture */
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;
    
    /** User's preferred language locale */
    @Column(name = "locale")
    private String locale;

    /** User's timezone preference */
    @Column(name = "timezone")
    private String timezone;

    /** Timestamp when the user accepted the terms of service */
    @Column(name = "terms_accepted_at", nullable = false)
    private LocalDateTime termsAcceptedAt;

    /** Timestamp when the user accepted the privacy policy */
    @Column(name = "privacy_policy_accepted_at", nullable = false)
    private LocalDateTime privacyPolicyAcceptedAt;

    /** Unique identifier linking to the authentication account */
    @Column(name = "account_id", nullable = false, unique = true)
    private UUID accountId;
    
    /** Timestamp when the user record was first created in the system */
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Timestamp when the user record was last modified */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}