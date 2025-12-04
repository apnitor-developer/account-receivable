package com.example.account.receivable.Customer.Dto.CustomerUpdateDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerEftUpdateDTO {

    private String bankName;
    private String ibanAccountNumber;
    private String bankIdentifierCode;
    private Boolean enableAchPayments;
}
