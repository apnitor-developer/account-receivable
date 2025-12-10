package com.example.account.receivable.Payment.Dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class InvoicePaymentDto {
    private Long invoiceId;
    private BigDecimal amount;
}
