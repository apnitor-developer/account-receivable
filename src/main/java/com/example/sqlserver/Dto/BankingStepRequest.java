package com.example.sqlserver.Dto;

import java.util.List;

import lombok.Data;

@Data
public class BankingStepRequest {
    private PaymentSettingsRequest paymentSettings;
    private List<BankAccountRequest> bankAccounts;
}

