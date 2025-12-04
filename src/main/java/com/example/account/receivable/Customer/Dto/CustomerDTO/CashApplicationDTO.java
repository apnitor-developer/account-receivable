package com.example.account.receivable.Customer.Dto.CustomerDTO;

import lombok.Data;

@Data
public class CashApplicationDTO {

    private Long customerId;
    private boolean applyPayments;
    private boolean autoApplyPayments;
    private Double toleranceAmount;
    private Double tolerancePercentage;
    private boolean shipCreditCheck;
    
}
