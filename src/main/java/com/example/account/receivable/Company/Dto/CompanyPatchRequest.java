package com.example.account.receivable.Company.Dto;

import java.util.List;
import lombok.Data;

@Data
public class CompanyPatchRequest {

    // top-level company fields
    private String legalName;
    private String tradeName;
    private String companyCode;
    private String country;
    private String baseCurrency;
    private String timeZone;

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

    // nested sections – names MUST match your JSON

    private FinancialSettingsRequest financial;       // "financial": { ... }

    private PaymentSettingsRequest payment;           // "payment": { ... }

    private List<BankAccountRequest> bankAccounts;    // "bankAccounts": [ ... ]

    private List<CompanyUserRequest> users;           // "users": [ ... ]
}

