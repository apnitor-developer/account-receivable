package com.example.account.receivable.CreditMemo.Dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class UpdateCreditMemoRequest {

    private String creditReason;

    private BigDecimal amount;

    private String currency;

    private Long arCodeId;

    private Long invoiceId;   // target invoice
}