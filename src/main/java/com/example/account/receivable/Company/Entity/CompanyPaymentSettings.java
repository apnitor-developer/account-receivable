package com.example.account.receivable.Company.Entity;


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
    private Long id;

    @OneToOne
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private Company company;

    @Column(name = "accept_check")
    private Boolean acceptCheck;

    @Column(name = "accept_credit_card")
    private Boolean acceptCreditCard;

    @Column(name = "accept_bank_transfer")
    private Boolean acceptBankTransfer;

    @Column(name = "accept_cash")
    private Boolean acceptCash;

    @Column(name = "remittance_instructions", columnDefinition = "TEXT")
    private String remittanceInstructions;
}

