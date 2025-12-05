package com.example.account.receivable.Company.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.account.receivable.Customer.Entity.Customer;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic information
    @Column(name = "legal_name", nullable = false)
    private String legalName;

    @Column(name = "trade_name")
    private String tradeName;

    @Column(name = "company_code")
    private String companyCode;

    @Column(name = "base_currency")
    private String baseCurrency; // e.g. "INR"

    @Column(name = "time_zone")
    private String timeZone;    // e.g. "Asia/Kolkata"

    @Column(name = "country")
    private String country;     // e.g. "IN"

    @Builder.Default
    @JsonIgnore
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Relationships
    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CompanyFinancialSettings financialSettings;

    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CompanyPaymentSettings paymentSettings;

    @OneToOne(mappedBy = "company" , cascade = CascadeType.ALL)
    private CompanyAddress companyAddress;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<Customer> customer;

    @OneToMany(mappedBy = "company" , cascade = CascadeType.ALL)
    private List<CompanyBankAccount> bankAccounts;

    @OneToMany(mappedBy = "company" , cascade = CascadeType.ALL)
    private List<CompanyUser> users;

}

