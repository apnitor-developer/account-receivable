package com.example.account.receivable.PaymentTerms.Dto;

import lombok.Data;

@Data
public class CreatePaymentTermRequest {

    private String name;
    private Integer netDays;
}
