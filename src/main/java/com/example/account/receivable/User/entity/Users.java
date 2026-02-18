package com.example.account.receivable.User.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.example.account.receivable.Company.Entity.UserCompany;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;
import lombok.Builder.Default;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    // One-to-many relationship with UserCompany
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCompany> userCompanies = new ArrayList<>();

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = true, length = 150)
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "password_changed_at", nullable = true)
    private Instant passwordChangedAt;

    @Default
    @Column(name = "force_password_change", nullable = true)
    private boolean forcePasswordChange = false;

    @Default
    @Column(nullable = true)
    private boolean deleted = false;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserRole> userRoles = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Default
    @Column(name = "mfa_enabled", nullable = true)
    private boolean mfaEnabled = false;

    @Column(name = "mfa_secret", length = 512)
    @JsonIgnore
    private String mfaSecret;

    @Column(name = "mfa_secret_temp", length = 512)
    @JsonIgnore
    private String mfaSecretTemp;

    @Column(name = "mfa_email_otp", length = 10)
    @JsonIgnore
    private String mfaEmailOtp;

    @Column(name = "mfa_email_otp_expires_at")
    @JsonIgnore
    private Instant mfaEmailOtpExpiresAt;

    @Default
    @Column(name = "mfa_email_verified", nullable = true)
    private boolean mfaEmailVerified = false;

}



