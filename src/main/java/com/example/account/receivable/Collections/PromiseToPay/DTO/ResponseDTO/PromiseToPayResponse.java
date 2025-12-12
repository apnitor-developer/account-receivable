package com.example.account.receivable.Collections.PromiseToPay.DTO.ResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.account.receivable.Collections.PromiseToPay.Entity.PromiseStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromiseToPayResponse {

    private Long id;
    private String customerName;
    private String invoiceNumber;
    private BigDecimal amountPromised;
    private LocalDate promiseDate;
    private PromiseStatus status;
    private String notes;
}
