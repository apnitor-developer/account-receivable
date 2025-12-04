package com.example.account.receivable.Customer.Dto.CustomerUpdateDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerVatUpdateDTO {

    private String taxIdentificationNumber;
    private String taxAgencyName;
    private Boolean enableVatCodes;
}
