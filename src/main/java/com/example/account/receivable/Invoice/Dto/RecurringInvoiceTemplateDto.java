package com.example.account.receivable.Invoice.Dto;

import java.time.LocalDate;
import java.util.List;

import com.example.account.receivable.Invoice.Enum.RecurringFrequency;

import lombok.Data;

@Data
public class RecurringInvoiceTemplateDto {

    private Long customerId;

    private LocalDate startDate;

    private RecurringFrequency frequency; 

    private Integer endAfter;

    private List<InvoiceItemDto> items;
}
