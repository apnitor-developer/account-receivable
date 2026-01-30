package com.example.account.receivable.Company.Dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.example.account.receivable.Common.Enum.CurrencyEnum;
import com.example.account.receivable.Company.Entity.*;
import com.example.account.receivable.User.entity.Users;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDetailsResponse {

    // Company core fields
    private Long id;
    private String legalName;
    private String tradeName;
    private String companyCode;
    private String country;
    private String baseCurrency;
    private String timeZone;

    // Nested financial
    private FinancialDto financial;

    // Nested payment
    private PaymentDto payment;

    // List of bank accounts
    private List<BankAccountDto> bankAccounts;
    private List<UsersDto> users;

    // ---- Nested DTOs ----

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FinancialDto {
        private Integer fiscalYearStartMonth;
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentDto {
        private Boolean acceptCheck;
        private Boolean acceptCreditCard;
        private Boolean acceptBankTransfer;
        private Boolean acceptCash;
        private String remittanceInstructions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BankAccountDto {
        private Long id;
        private String bankName;
        private String accountNumber;
        private String ifscSwift;
        private CurrencyEnum currency;
        private Boolean isDefault;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressDTO {
        private Long id;
        private String addressLine1;
        private String city;
        private String stateProvince;
        private String postalCode;
        private String addressCountry;

        private String primaryContactName;
        private String primaryContactEmail;
        private String primaryContactPhone;

        private String website;
        private String primaryContactCountry;
    }

    // Factory method to build response from entities
    public static CompanyDetailsResponse fromEntities(
            Company company,
            CompanyAddress address,
            CompanyFinancialSettings financial,
            CompanyPaymentSettings payment,
            List<CompanyBankAccount> accounts,
             List<Users> users
    ) {
        FinancialDto financialDto = null;
        if (financial != null) {
            financialDto = FinancialDto.builder()
                    .fiscalYearStartMonth(financial.getFiscalYearStartMonth())
                    .defaultArAccountCode(financial.getDefaultArAccountCode())
                    .revenueRecognitionMode(financial.getRevenueRecognitionMode())
                    .defaultTaxHandling(financial.getDefaultTaxHandling())
                    .defaultPaymentTerms(financial.getDefaultPaymentTerms())
                    .allowOtherTerms(financial.getAllowOtherTerms())
                    .enableCreditLimitChecking(financial.getEnableCreditLimitChecking())
                    .agingBucketConfig(financial.getAgingBucketConfig())
                    .dunningFrequencyDays(financial.getDunningFrequencyDays())
                    .enableAutomatedDunningEmails(financial.getEnableAutomatedDunningEmails())
                    .defaultCreditLimit(financial.getDefaultCreditLimit())
                    .build();
        }

        PaymentDto paymentDto = null;
        if (payment != null) {
            paymentDto = PaymentDto.builder()
                    .acceptCheck(payment.getAcceptCheck())
                    .acceptCreditCard(payment.getAcceptCreditCard())
                    .acceptBankTransfer(payment.getAcceptBankTransfer())
                    .acceptCash(payment.getAcceptCash())
                    .remittanceInstructions(payment.getRemittanceInstructions())
                    .build();
        }


        List<BankAccountDto> accountDtos =
                accounts == null ? List.of() :
                        accounts.stream()
                                .map(a -> BankAccountDto.builder()
                                        .id(a.getId())
                                        .bankName(a.getBankName())
                                        .accountNumber(a.getAccountNumber())
                                        .ifscSwift(a.getIfscSwift())
                                        .currency(a.getCurrency())
                                        .isDefault(a.getIsDefault())
                                        .build())
                                .collect(Collectors.toList());


            if (address != null) {
            }

        return CompanyDetailsResponse.builder()
                .id(company.getId())
                .legalName(company.getLegalName())
                .tradeName(company.getTradeName())
                .companyCode(company.getCompanyCode())
                .country(company.getCountry())
                .baseCurrency(company.getBaseCurrency())
                .timeZone(company.getTimeZone())
                .financial(financialDto)
                .payment(paymentDto)
                .bankAccounts(accountDtos)
                .build();
    }
}
