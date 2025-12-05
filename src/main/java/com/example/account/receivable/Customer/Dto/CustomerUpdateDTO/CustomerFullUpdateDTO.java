package com.example.account.receivable.Customer.Dto.CustomerUpdateDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerFullUpdateDTO {

    // MAIN CUSTOMER DATA (all nullable)
    private String customerName;
    private Long customerId;
    private String email;
    private String customerType;

    // ADDRESS (if present → replace addresses)
    private CustomerAddressUpdateDTO addresses;

    // Nested sections (if present → patch those parts)
    private CashApplicationUpdateDTO cashApplication;
    private CustomerStatementUpdateDTO statement;
    private CustomerEftUpdateDTO eft;
    private CustomerVatUpdateDTO vat;
    private CustomerDunningCreditSettingsUpdateDTO dunningCredit;
}
