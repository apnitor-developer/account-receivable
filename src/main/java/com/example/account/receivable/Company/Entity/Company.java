package com.example.account.receivable.Company.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;

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

    @Column(name = "country")
    private String country;     // e.g. "IN"

    @Column(name = "base_currency")
    private String baseCurrency; // e.g. "INR"

    @Column(name = "time_zone")
    private String timeZone;    // e.g. "Asia/Kolkata"

    // Head office address
    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "city")
    private String city;

    @Column(name = "state_province")
    private String stateProvince;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "address_country")
    private String addressCountry;

    // Primary contact
    @Column(name = "primary_contact_name")
    private String primaryContactName;

    @Column(name = "primary_contact_email")
    private String primaryContactEmail;

    @Column(name = "primary_contact_phone")
    private String primaryContactPhone;

    @Column(name = "website")
    private String website;

    @Column(name = "primary_contact_country")
    private String primaryContactCountry;

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
}

