package com.example.account.receivable.Customer.Dto.CustomerDTO;

import java.util.List;
import lombok.Data;

@Data
public class CustomerFullRequestDTO {

    // MAIN CUSTOMER DATA
    private String customerName;
    private String customerId;
    private String email;
    private String customerType;

    // ADDRESS (multiple)
    private List<CustomerAddressDTO> addresses;

    // CASH APPLICATION
    private CashApplicationDTO cashApplication;

    // STATEMENT
    private CustomerStatementDTO statement;

    // EFT
    private CustomerEftDTO eft;

    // VAT
    private CustomerVatDTO vat;

    // DUNNING & CREDIT
    private CustomerDunningCreditSettingsDTO dunningCredit;
}
