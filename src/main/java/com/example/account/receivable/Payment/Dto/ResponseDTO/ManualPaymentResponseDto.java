package com.example.account.receivable.Payment.Dto.ResponseDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.example.account.receivable.BankReconciliation.Enum.PaymentStatus;
import com.example.account.receivable.Payment.Enum.PaymentMethod;
import com.example.account.receivable.Payment.Enum.PaymentSource;

import lombok.Data;

@Data
public class ManualPaymentResponseDto {

    private Long paymentId;

    private BigDecimal bankDeposit;
    private BigDecimal serviceFee;
    private BigDecimal paymentAmount;

    private PaymentMethod paymentMethod;
    private PaymentSource source;
    private PaymentStatus status;

    private LocalDate paymentDate;
    private String notes;

    private Long customerId;
    private String customerName;

    private Instant createdAt;

}