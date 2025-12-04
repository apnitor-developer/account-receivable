package com.example.account.receivable.Company.Dto;

import lombok.Data;

@Data
public class PaymentSettingsRequest {
    private Boolean acceptCheck;
    private Boolean acceptCreditCard;
    private Boolean acceptBankTransfer;
    private Boolean acceptCash;
    private String remittanceInstructions;
}

