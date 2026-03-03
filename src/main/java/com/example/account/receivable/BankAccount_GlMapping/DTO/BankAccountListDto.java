package com.example.account.receivable.BankAccount_GlMapping.DTO;

import com.example.account.receivable.BankAccount_GlMapping.Enum.BankGlMappingStatus;
import com.example.account.receivable.Common.Enum.CurrencyEnum;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankAccountListDto {

    private Long bankAccountId;
    private String bankName;
    private String accountNumber;
    private String address;
    private String branch;
    private CurrencyEnum currency;
    private Boolean isDefault;

    private BankGlMappingStatus mappingStatus;
}

