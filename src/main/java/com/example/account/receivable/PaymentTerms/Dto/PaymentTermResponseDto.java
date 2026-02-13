package com.example.account.receivable.PaymentTerms.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentTermResponseDto {

    private Long id;
    private String name;
    private Integer netDays;
    private Boolean active;
    private Boolean systemDefined;
}
