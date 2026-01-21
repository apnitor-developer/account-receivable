package com.example.account.receivable.GL.Dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.example.account.receivable.GL.Enum.GlReferenceType;
import com.example.account.receivable.GL.Enum.GlTransactionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlTransactionResponse {
    private Long id;
    private Long companyId;
    private String companyName;
    // private Long arCodeId;
    // private String arCode;
    private GlReferenceType referenceType;
    private Long referenceId;
    private String referenceNumber;
    private LocalDate transactionDate;
    private BigDecimal amount;
    private GlTransactionStatus status;
    private String description;
    private Long createdBy;
    private Instant createdAt;
    private List<GlTransactionLineDto> lines;
}
