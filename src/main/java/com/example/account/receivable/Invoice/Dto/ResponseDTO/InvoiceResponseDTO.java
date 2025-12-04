package com.example.account.receivable.Invoice.Dto.ResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.example.account.receivable.Customer.Dto.ResponseDTO.CustomerMiniDTO;

public class InvoiceResponseDTO {
    private Long id;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private BigDecimal totalAmount;
    private CustomerMiniDTO customer;
}
