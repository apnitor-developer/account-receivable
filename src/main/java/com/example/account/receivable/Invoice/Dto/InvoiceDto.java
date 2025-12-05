package com.example.account.receivable.Invoice.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class InvoiceDto {

    @JsonProperty("isGenerated")
    private Boolean generated;
    // Header fields
    private String invoiceNumber;     
    private LocalDate invoiceDate;    
    private LocalDate dueDate; 
    private String note;

    private BigDecimal subTotal;

    private BigDecimal taxAmount;

    private BigDecimal totalAmount;

    // Line items
    // private List<InvoiceItemDto> items;
}
