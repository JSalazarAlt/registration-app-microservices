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

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    // ----------------------------------------------------------------
    // ACCOUNT'S CREDENTIALS (Mirrored from Auth microservice)
    // ----------------------------------------------------------------

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    // ----------------------------------------------------------------
    // PROFILE
    // ----------------------------------------------------------------

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    // ----------------------------------------------------------------
    // PREFERENCES
    // ----------------------------------------------------------------
    
    @Column(name = "locale")
    private String locale;

    @Column(name = "timezone")
    private String timezone;

    // ----------------------------------------------------------------
    // LEGAL TERMS
    // ----------------------------------------------------------------

    @Column(name = "terms_accepted_at", nullable = false)
    private Instant termsAcceptedAt;

    @Column(name = "privacy_policy_accepted_at", nullable = false)
    private Instant privacyPolicyAcceptedAt;

    // ----------------------------------------------------------------
    // STATUS
    // ----------------------------------------------------------------

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    // ----------------------------------------------------------------
    // AUDITORY
    // ----------------------------------------------------------------
    
    @CreatedDate
    @Column(name = "created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "soft_deleted_at")
    private Instant softDeletedAt;

    // ----------------------------------------------------------------
    // RELATIONSHIPS
    // ----------------------------------------------------------------

    @Column(name = "account_id", nullable = false, unique = true)
    private UUID accountId;

}