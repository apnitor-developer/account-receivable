package com.example.account.receivable.Payment.Dto.ResponseDTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthlyPaymentDto {
    private String month;
    private BigDecimal totalAmount;
}
