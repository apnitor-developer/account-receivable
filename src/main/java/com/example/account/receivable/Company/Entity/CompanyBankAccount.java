package com.example.account.receivable.Company.Entity;


import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.account.receivable.Common.Enum.CurrencyEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_bank_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyBankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "branch")
    private String branch; 

    @Column(name = "ifsc_swift")
    private String ifscSwift;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency")
    private CurrencyEnum currency; // e.g. "USD" , "GBP"

    @Column(name = "is_default")
    private Boolean isDefault;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}

