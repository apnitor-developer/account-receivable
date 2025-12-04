package com.example.account.receivable.Customer.Dto.CustomerUpdateDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerDunningCreditSettingsUpdateDTO {

    private Boolean placeOnCreditHold;
    private Double creditLimit;
    private String dunningLevel;
    private String pastDue;
    private String level1;
    private String level2;
    private String level3;
    private String level4;
}
