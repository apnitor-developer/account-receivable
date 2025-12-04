package com.example.sqlserver.Entity;


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
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "ifsc_swift")
    private String ifscSwift;

    @Column(name = "currency")
    private String currency; // e.g. "INR"

    @Column(name = "is_default")
    private Boolean isDefault;
}

