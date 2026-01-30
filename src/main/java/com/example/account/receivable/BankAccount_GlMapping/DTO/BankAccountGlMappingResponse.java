package com.example.account.receivable.BankAccount_GlMapping.DTO;

import java.time.LocalDate;

import com.example.account.receivable.BankAccount_GlMapping.Enum.MappingStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankAccountGlMappingResponse {
    private Long id;
    private Long bankAccountId;
    private String bankName;
    private Long glCodeId;
    private String glCode;
    private MappingStatus status;
    private LocalDate effectiveFrom;
}

