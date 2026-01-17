package com.example.account.receivable.Invoice.Dto.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvoiceStatusBreakdownResponseDto {
    private Long open;
    private Long partial;
    private Long paid;
    private Long writtenOff;
    private Long total;
}
