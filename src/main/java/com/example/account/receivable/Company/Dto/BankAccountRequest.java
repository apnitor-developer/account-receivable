package com.example.account.receivable.Company.Dto;

import lombok.Data;

@Data
public class BankAccountRequest {
    private String bankName;
    private String accountNumber;
    private String ifscSwift;
    private String currency;
    private Boolean isDefault;
}

