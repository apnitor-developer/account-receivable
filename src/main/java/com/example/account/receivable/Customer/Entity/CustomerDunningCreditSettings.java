package com.example.account.receivable.Customer.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CustomerDunningCreditSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean placeOnCreditHold;
    private Double creditLimit;
    private String dunningLevel;
    private String pastDue;
    private String level1;
    private String level2;
    private String level3;
    private String level4;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}
