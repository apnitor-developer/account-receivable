package com.example.account.receivable.Invoice.Dto.ResponseDTO;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CustomerWithPendingAmountResponseDTO {
    private Long id;
    private String customerName;
    private BigDecimal overdueAmount;
}
