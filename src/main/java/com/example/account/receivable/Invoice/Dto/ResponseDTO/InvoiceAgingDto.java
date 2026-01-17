package com.example.account.receivable.Invoice.Dto.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvoiceAgingDto {
    private Long current;
    private Long days0to30;
    private Long days31to60;
    private Long days61to90;
    private Long days90Plus;
}

