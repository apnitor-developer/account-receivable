package com.example.account.receivable.Customer.Dto.CustomerDTO;

import lombok.Data;

@Data
public class CustomerEftDTO {
    private Long customerId;
    private String bankName;
    private String ibanAccountNumber;
    private String bankIdentifierCode;
    private boolean enableAchPayments;
}
