package com.example.account.receivable.Invoice.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class OverdueInvoiceResponseDTO {

    private Long invoiceId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;

    private String status;
    private BigDecimal totalAmount;
    private BigDecimal balanceDue;

    private Long customerId;
    private String customerName;
}
