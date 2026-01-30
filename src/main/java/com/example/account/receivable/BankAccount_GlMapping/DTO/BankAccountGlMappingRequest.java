package com.example.account.receivable.BankAccount_GlMapping.DTO;

import java.time.LocalDate;

import com.example.account.receivable.BankAccount_GlMapping.Enum.MappingStatus;

import lombok.Data;

@Data
public class BankAccountGlMappingRequest {
    private Long bankAccountId;
    private Long glCodeId;
    private LocalDate effectiveFrom; // optional
    private MappingStatus status; 
}

