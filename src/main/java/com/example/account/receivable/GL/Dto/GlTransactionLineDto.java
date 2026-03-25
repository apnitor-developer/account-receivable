package com.example.account.receivable.GL.Dto;

import java.math.BigDecimal;

import com.example.account.receivable.GL.Enum.GlEntryType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlTransactionLineDto {
    // private Long glCodeId;
    private String glCode;
    private String glCodeDescription;
    private GlEntryType entryType;
    private BigDecimal amount;
}
