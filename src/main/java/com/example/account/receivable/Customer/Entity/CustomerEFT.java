package com.example.account.receivable.Customer.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Table(name = "customer_eft")
@Entity
@Data
public class CustomerEFT {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bankName;
    private String ibanAccountNumber;
    private String bankIdentifierCode;

    private boolean enableAchPayments;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}
