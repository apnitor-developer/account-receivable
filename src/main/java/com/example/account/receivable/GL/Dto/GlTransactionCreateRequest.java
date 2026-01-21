package com.example.account.receivable.GL.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.account.receivable.GL.Enum.GlReferenceType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlTransactionCreateRequest {


    private Long referenceId;
    private String referenceNumber;

    @NotNull
    private GlReferenceType referenceType;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private LocalDate transactionDate;
    private String description;
}
