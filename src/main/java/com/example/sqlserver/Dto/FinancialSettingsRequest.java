package com.example.sqlserver.Dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class FinancialSettingsRequest {
    private Integer fiscalYearStartMonth;   // 1−12
    private String defaultArAccountCode;
    private String revenueRecognitionMode;
    private String defaultTaxHandling;
    private String defaultPaymentTerms;
    private Boolean allowOtherTerms;
    private Boolean enableCreditLimitChecking;
    private String agingBucketConfig;
    private Integer dunningFrequencyDays;
    private Boolean enableAutomatedDunningEmails;
    private BigDecimal defaultCreditLimit;
}

