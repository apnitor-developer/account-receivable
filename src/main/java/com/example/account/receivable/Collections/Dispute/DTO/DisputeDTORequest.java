package com.example.account.receivable.Collections.Dispute.DTO;

import lombok.Data;

@Data
public class DisputeDTORequest {
    private Long customerId;
    private Long invoiceId;
    private String disputeCode;
    private String reason;
    private String disputedAmount;
}
