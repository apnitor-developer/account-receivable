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

    // nested sections – names MUST match your JSON

    private AddressRequestDto address;
    private FinancialSettingsRequest financial;       // "financial": { ... }

    private PaymentSettingsRequest payment;           // "payment": { ... }

    private List<BankAccountRequest> bankAccounts;    // "bankAccounts": [ ... ]

    private List<CompanyUserRequest> users;           // "users": [ ... ]
}

