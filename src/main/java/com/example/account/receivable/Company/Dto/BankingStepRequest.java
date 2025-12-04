package com.example.account.receivable.Company.Dto;

import java.util.List;

import lombok.Data;

@Data
public class BankingStepRequest {
    private PaymentSettingsRequest paymentSettings;
    private List<BankAccountRequest> bankAccounts;
}

