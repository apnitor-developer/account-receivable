package com.example.account.receivable.CreditMemo.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class CreateCreditMemoRequest {
    private String creditReason;
    private LocalDate creditMemoDate;
    private BigDecimal amount;
    private String currency;
    private Long arCodeId; // optional
    private Long invoiceId;

    private List<Long> referenceInvoiceIds;
}

