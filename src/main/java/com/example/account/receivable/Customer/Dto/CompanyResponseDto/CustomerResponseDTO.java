package com.example.account.receivable.Customer.Dto.CompanyResponseDto;

import java.time.Instant;

import com.example.account.receivable.Customer.Entity.CashApplication;
import com.example.account.receivable.Customer.Entity.CustomerAddress;
import com.example.account.receivable.Customer.Entity.CustomerDunningCreditSettings;
import com.example.account.receivable.Customer.Entity.CustomerEFT;
import com.example.account.receivable.Customer.Entity.CustomerStatement;
import com.example.account.receivable.Customer.Entity.CustomerVAT;

import lombok.Data;

@Data
public class CustomerResponseDTO {
    private Long id;
    private String customerName;
    private Long customerId;
    private String email;
    private String customerType;
    private boolean deleted;
    private Instant createdAt;
    private Instant updatedAt;

    // All relations
    private CustomerAddress address;
    private CashApplication cashApplication;
    private CustomerStatement statement;
    private CustomerEFT eft;
    private CustomerVAT vat;
    private CustomerDunningCreditSettings dunning;

    // Extra field you want
    private Long companyId;
    private String companyName;
}
