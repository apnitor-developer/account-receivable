package com.example.account.receivable.GLCodes.Dto;

import com.example.account.receivable.GLCodes.Enum.GlAccountType;

import lombok.Data;

@Data
public class GlCodeDto {

    private String glCode;

    private String description;

    private GlAccountType accountType;

    private Boolean isActive;
}
