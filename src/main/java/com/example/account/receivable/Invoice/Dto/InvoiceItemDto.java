package com.example.account.receivable.Invoice.Dto;

import lombok.Data;

@Data
public class InvoiceItemDto {
    private Long productId;
    private String description;   // "Item description"
    private Integer quantity;     // "1"
    private String taxType;       // "None" (dropdown value)
}
