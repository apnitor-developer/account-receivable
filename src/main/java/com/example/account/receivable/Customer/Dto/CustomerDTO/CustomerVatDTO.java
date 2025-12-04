package com.example.account.receivable.Customer.Dto.CustomerDTO;

import lombok.Data;

@Data
public class CustomerVatDTO {
    private Long customerId;
    private String taxIdentificationNumber;
    private String taxAgencyName;
    private boolean enableVatCodes;
}
