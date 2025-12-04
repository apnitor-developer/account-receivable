package com.example.account.receivable.Customer.Dto.CustomerUpdateDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CashApplicationUpdateDTO {

    private Boolean applyPayments;
    private Boolean autoApplyPayments;
    private Double toleranceAmount;
    private Double tolerancePercentage;
    private Boolean shipCreditCheck;
}
