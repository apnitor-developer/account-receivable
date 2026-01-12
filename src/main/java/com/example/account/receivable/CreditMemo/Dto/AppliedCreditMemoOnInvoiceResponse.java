package com.example.account.receivable.CreditMemo.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class AppliedCreditMemoOnInvoiceResponse {
    
    private Long applicationId;

    private Long creditMemoId;
    private String creditMemoNo;
    private String creditReason;
    private String currency;
    private LocalDate postingDate;

    private BigDecimal appliedAmount;
    private LocalDate appliedDate;

    private Long invoiceId;
    private String invoiceNumber;

    private Long customerId;
    private String customerName;

    // optional (if you want)
    private Long arCodeId;
    private String arCode;
    private String arCodeName;
}
