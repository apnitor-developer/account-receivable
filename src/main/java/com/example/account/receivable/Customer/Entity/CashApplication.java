package com.example.account.receivable.Customer.Entity;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@Data
public class CashApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean applyPayments;
    private boolean autoApplyPayments;
    private Double toleranceAmount;
    private Double tolerancePercentage;
    private boolean shipCreditCheck;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}
