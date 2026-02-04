package com.example.account.receivable.Customer.Dto.CompanyResponseDto;

import java.time.Instant;

import com.example.account.receivable.Customer.Entity.CashApplication;
import com.example.account.receivable.Customer.Entity.CustomerAddress;
import com.example.account.receivable.Customer.Entity.CustomerDunningCreditSettings;
import com.example.account.receivable.Customer.Entity.CustomerEFT;
import com.example.account.receivable.Customer.Entity.CustomerStatement;
import com.example.account.receivable.Customer.Entity.CustomerVAT;
import com.example.account.receivable.Customer.Enum.CustomerTypeEnum;

import lombok.Data;

@Data
public class CustomerResponseDTO {
    private Long id;
    private String customerName;
    private Long customerId;
    private String email;
    private CustomerTypeEnum customerType;
    private String phoneNumber;
    private String linkedin;
    private String faceBook;
    private String twitter;
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
