package com.example.account.receivable.CreditMemo.Dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerCreditBalanceResponse {

    private Long customerId;
    private String customerName;

    private BigDecimal totalPostedCredit;
    private BigDecimal totalAppliedCredit;
    private BigDecimal availableCredit;

    private String currency; // optional
}
