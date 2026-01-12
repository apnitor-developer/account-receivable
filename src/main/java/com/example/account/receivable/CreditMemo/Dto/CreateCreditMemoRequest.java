package com.example.account.receivable.CreditMemo.Dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CreateCreditMemoRequest {
    private String creditReason;
    private BigDecimal amount;
    private String currency;
    private Long arCodeId; // optional
    private Long invoiceId;
}

