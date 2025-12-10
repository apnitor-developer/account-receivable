package com.example.account.receivable.Payment.Dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
// public class ReceivePaymentRequest {

//     private BigDecimal paymentAmount;
//     private String paymentMethod;
//     private String notes;
//     private List<InvoicePaymentDto> invoicePayments;
// }
public class ReceivePaymentRequest {
    private BigDecimal paymentAmount;
    private String paymentMethod;
    private String notes;
    private List<Long> invoiceIds; // only IDs, backend will auto apply
}

