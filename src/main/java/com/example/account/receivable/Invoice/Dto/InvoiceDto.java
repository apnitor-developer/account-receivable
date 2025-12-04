package com.example.account.receivable.Invoice.Dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class InvoiceDto {
    // Header fields
    private String invoiceNumber;     
    private LocalDate invoiceDate;    
    private LocalDate dueDate; 
    private String note;

    // Line items
    private List<InvoiceItemDto> items;
}
