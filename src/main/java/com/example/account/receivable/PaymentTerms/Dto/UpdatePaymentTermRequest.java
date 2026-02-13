package com.example.account.receivable.PaymentTerms.Dto;

import lombok.Data;

@Data
public class UpdatePaymentTermRequest {

    private String name;
    private Integer netDays;
    private Boolean active;
}
