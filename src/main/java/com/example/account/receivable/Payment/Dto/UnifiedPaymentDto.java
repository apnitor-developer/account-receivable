package com.example.account.receivable.Payment.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnifiedPaymentDto {

    private Long id;

    private LocalDate date;

    private BigDecimal amount;

    private String customerName;

    private String description;

    /** MANUAL or BAI */
    private String source;

    /** DRAFT / APPROVED etc */
    private String status;
}

