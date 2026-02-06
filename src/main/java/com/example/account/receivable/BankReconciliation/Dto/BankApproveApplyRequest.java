package com.example.account.receivable.BankReconciliation.Dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BankApproveApplyRequest {
    
    @NotNull(message = "customerId is required")
    private Long customerId;

    @NotEmpty(message = "At least one invoice must be selected")
    private List<Long> invoiceIds;

}
