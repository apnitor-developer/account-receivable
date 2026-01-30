package com.example.account.receivable.Company.Dto;

import com.example.account.receivable.Common.Enum.CurrencyEnum;

import lombok.Data;

@Data
public class BankAccountRequest {
    private String bankName;
    private String accountNumber;
    private String ifscSwift;
    private CurrencyEnum currency;
    private Boolean isDefault;
}

