package com.example.account.receivable.Customer.Dto.CustomerDTO;

import lombok.Data;

@Data
public class CustomerDunningCreditSettingsDTO {
    private Long customerId;
    private boolean placeOnCreditHold;
    private Double creditLimit;
    private String dunningLevel;
    private String pastDue;
    private String level1;
    private String level2;
    private String level3;
    private String level4;
}
