package com.example.account.receivable.Customer.Dto.CustomerUpdateDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerStatementUpdateDTO {

    private Boolean sendStatements;
    private Boolean autoApplyPayments;
    private Double tolerancePercentage;
    private Double minimumAmount;
}
