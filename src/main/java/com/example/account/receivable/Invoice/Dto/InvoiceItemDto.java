package com.example.account.receivable.Invoice.Dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class InvoiceItemDto {
    private String itemName;
    private BigDecimal rate;
    private String description;
    private Integer quantity;     
    private String tax;             // "10%" or "None"
}
