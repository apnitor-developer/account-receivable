package com.example.account.receivable.Company.Entity;


import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_payment_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyPaymentSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary Key

    @Column(name = "accept_check")
    private Boolean acceptCheck;

    @Column(name = "accept_credit_card")
    private Boolean acceptCreditCard;

    @Column(name = "accept_bank_transfer")
    private Boolean acceptBankTransfer;

    @Column(name = "accept_cash")
    private Boolean acceptCash;

    @Column(name = "remittance_instructions")
    private String remittanceInstructions;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private Company company;
}

