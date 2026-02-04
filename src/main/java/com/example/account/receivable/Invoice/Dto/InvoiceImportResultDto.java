package com.example.account.receivable.Invoice.Dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceImportResultDto {
    private int totalRows;
    private int successCount;
    private int failureCount;
    private List<RowErrorDto> errors;
}
