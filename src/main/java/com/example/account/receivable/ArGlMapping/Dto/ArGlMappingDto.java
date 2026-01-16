package com.example.account.receivable.ArGlMapping.Dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArGlMappingDto {

    @NotNull
    private Long arCodeId;

    @NotNull
    private Long debitGlCodeId;

    @NotNull
    private Long creditGlCodeId;
}
