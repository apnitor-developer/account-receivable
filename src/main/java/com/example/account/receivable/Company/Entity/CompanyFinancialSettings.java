package com.example.account.receivable.Company.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "company_financial_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyFinancialSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private Company company;

    @Column(name = "fiscal_year_start_month")
    private Integer fiscalYearStartMonth; // 1 = January, 2 = February...

    @Column(name = "default_ar_account_code")
    private String defaultArAccountCode;

    @Column(name = "revenue_recognition_mode")
    private String revenueRecognitionMode; // e.g. "ON_INVOICE"

    @Column(name = "default_tax_handling")
    private String defaultTaxHandling; // e.g. "LINE_ITEM_LEVEL"

    @Column(name = "default_payment_terms")
    private String defaultPaymentTerms; // e.g. "NET_30"

    @Column(name = "allow_other_terms")
    private Boolean allowOtherTerms;

    @Column(name = "enable_credit_limit_checking")
    private Boolean enableCreditLimitChecking;

    @Column(name = "aging_bucket_config")
    private String agingBucketConfig; // e.g. "0-30"

    @Column(name = "dunning_frequency_days")
    private Integer dunningFrequencyDays;

    @Column(name = "enable_automated_dunning_emails")
    private Boolean enableAutomatedDunningEmails;

    @Column(name = "default_credit_limit", precision = 18, scale = 2)
    private BigDecimal defaultCreditLimit;
}
