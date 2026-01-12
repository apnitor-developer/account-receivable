package com.example.account.receivable.WriteOff.Dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class CompanyWriteOffResponse {

    private Long Id;

    private Long invoiceId;
    private String invoiceNumber;

    private Long customerId;
    private String customerName;

    private String reason;

    private LocalDate writeOffDate;
}
