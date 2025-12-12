package com.example.account.receivable.Collections.PromiseToPay.DTO.RequestDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class PromiseToPayRequest {

    private Long customerId;
    private Long invoiceId; // optional
    private BigDecimal amountPromised;
    private LocalDate promiseDate;
    private String notes;
}
