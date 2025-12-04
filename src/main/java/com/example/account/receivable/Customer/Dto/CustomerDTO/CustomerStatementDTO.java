package com.example.account.receivable.Customer.Dto.CustomerDTO;

import lombok.Data;

@Data
public class CustomerStatementDTO {

    private boolean sendStatements;
    private boolean autoApplyPayments;
    private Double tolerancePercentage;
    private Double minimumAmount;
    private Long customerId;  // Link to Customer entity
}
