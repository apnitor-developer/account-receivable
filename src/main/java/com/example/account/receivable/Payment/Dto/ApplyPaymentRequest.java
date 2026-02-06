package com.example.account.receivable.Payment.Dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ApplyPaymentRequest {

    @NotEmpty(message = "At least one invoice must be selected")
    private List<Long> invoiceIds;
}
