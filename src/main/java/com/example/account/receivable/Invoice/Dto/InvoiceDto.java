package com.example.account.receivable.Invoice.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    //main invoice amount
    private BigDecimal rate;
    
    private BigDecimal taxAmount;

    // Line items
    // private List<InvoiceItemDto> items;
}
