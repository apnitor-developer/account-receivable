package com.example.account.receivable.CreditMemo.Dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ApplyCreditMemoRequest {
    private Long invoiceId;
    private BigDecimal amount;
}
