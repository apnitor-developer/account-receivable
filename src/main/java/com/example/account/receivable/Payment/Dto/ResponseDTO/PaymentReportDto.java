package com.example.account.receivable.Payment.Dto.ResponseDTO;

import java.util.Map;

import com.example.account.receivable.Payment.Enum.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentReportDto {
    private Map<PaymentMethod, Long> methodCounts;
    private Long totalPayments;
}
