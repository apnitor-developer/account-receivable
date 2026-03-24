package com.example.account.receivable.Auth.entity;

import java.time.Instant;

import com.example.account.receivable.User.entity.Users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_login_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which user logged in
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // Login timestamp
    @Column(name = "login_at", nullable = false)
    private Instant loginAt;

    // Optional: logout time
    @Column(name = "logout_at")
    private Instant logoutAt;

    // Optional: IP address
    @Column(name = "ip_address")
    private String ipAddress;

    // Optional: device/browser info
    @Column(name = "user_agent")
    private String userAgent;

    // Optional: success/failure
    @Column(name = "status")
    private String status; // SUCCESS / FAILED
}
