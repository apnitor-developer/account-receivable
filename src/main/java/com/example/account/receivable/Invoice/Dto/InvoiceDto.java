package com.example.account.receivable.Invoice.Dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class InvoiceDto {
    @JsonProperty("isGenerated")
    private Boolean generated;
    
    private String invoiceNumber;     
    private LocalDate invoiceDate;    
    private LocalDate dueDate; 
    private String note;

    private List<InvoiceItemDto> items;
}
