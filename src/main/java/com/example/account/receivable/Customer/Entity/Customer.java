package com.example.account.receivable.Customer.Entity;

import com.example.account.receivable.Invoice.Entity.Invoice;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(unique = true , nullable = false)
    private String email;

    private String customerType;

    @Column(nullable =  true)
    private boolean deleted = false;

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
}
