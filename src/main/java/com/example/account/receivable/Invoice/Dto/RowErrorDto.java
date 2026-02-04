package com.example.account.receivable.Invoice.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RowErrorDto {
    private long rowNumber;
    private String message;
}
