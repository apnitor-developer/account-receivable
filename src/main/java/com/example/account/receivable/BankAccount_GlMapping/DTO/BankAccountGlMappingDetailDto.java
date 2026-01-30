package com.example.account.receivable.BankAccount_GlMapping.DTO;

import com.example.account.receivable.BankAccount_GlMapping.Enum.MappingStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankAccountGlMappingDetailDto {

    private Long mappingId;
    private Long bankAccountId;
    private String bankName;
    private String bankNumber;

    private Long glCodeId;
    private String glCode;
    private String glDescription;

    private MappingStatus status;
}

