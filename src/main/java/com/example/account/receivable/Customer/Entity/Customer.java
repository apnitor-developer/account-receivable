package com.example.account.receivable.Customer.Entity;

import com.example.account.receivable.Customer.Enum.CustomerTypeEnum;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Data;


@Entity
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private Long customerId;

    @Column( nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "customerType", nullable = false)
    private CustomerTypeEnum customerType;

    private String phoneNumber;
    private String faceBook;
    private String linkedin;
    private String twitter;

    @Column(nullable =  true)
    private boolean deleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    //Relation Address
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CustomerAddress address ;

    //Relation Application
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CashApplication cashApplication;

    //Relation Statement
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CustomerStatement statement;

    //Relation EFT
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CustomerEFT eft;

    //Relation VAT
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CustomerVAT vat;

    //Relation DunningCredit
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CustomerDunningCreditSettings  dunning;
    
    @JsonIgnore
    @OneToMany(mappedBy = "customer")
    private List<Invoice> invoices;

    @JsonIgnore
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompanyCustomers> companyCompanies = new ArrayList<>();
}

