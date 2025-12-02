package com.suyos.userservice.model;

import java.time.Instant;
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
 * Entity representing a user.
 * 
 * <p>Maps to the <b>users</b> table and stores user profile, preferences,
 * and acceptance timestamps for legal agreements. Mirrors core identity
 * fields from the Auth microservice and links each user to its account.</p>
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

    // ----------------------------------------------------------------
    // IDENTITY
    // ----------------------------------------------------------------

    /** Unique identifier */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    // ----------------------------------------------------------------
    // RELATIONSHIPS
    // ----------------------------------------------------------------

    /** Unique identifier linking to account */
    @Column(name = "account_id", nullable = false, unique = true)
    private UUID accountId;

    // ----------------------------------------------------------------
    // ACCOUNT INFORMATION
    // ----------------------------------------------------------------

    /** Username (mirrored from Auth microservice) */
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    /** Email address (mirrored from Auth microservice) */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    // ----------------------------------------------------------------
    // PROFILE
    // ----------------------------------------------------------------

    /** First name */
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /** Last name */
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /** Phone number */
    @Column(name = "phone")
    private String phone;

    /** Profile picture URL */
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    // ----------------------------------------------------------------
    // PREFERENCES
    // ----------------------------------------------------------------
    
    /** Preferred language locale */
    @Column(name = "locale")
    private String locale;

    /** Preferred timezone */
    @Column(name = "timezone")
    private String timezone;

    // ----------------------------------------------------------------
    // LEGAL TERMS
    // ----------------------------------------------------------------

    /** Timestamp when terms of service were accepted */
    @Column(name = "terms_accepted_at", nullable = false)
    private Instant termsAcceptedAt;

    /** Timestamp when privacy policy was accepted */
    @Column(name = "privacy_policy_accepted_at", nullable = false)
    private Instant privacyPolicyAcceptedAt;

    // ----------------------------------------------------------------
    // STATUS
    // ----------------------------------------------------------------

    /** Flag indicating if user was soft deleted */
    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    /** Timestamp when user was soft deleted */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    // ----------------------------------------------------------------
    // AUDITORY
    // ----------------------------------------------------------------
    
    /** Timestamp when user record was first created */
    @CreatedDate
    @Column(name = "created_at")
    private Instant createdAt;

    /** Timestamp when user record was last modified */
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

}