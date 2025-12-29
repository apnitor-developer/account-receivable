package com.example.account.receivable.Collections.Dispute.DTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisputeStatusUpdateRequest {
    private String status;   // "OPEN", "UNDER_REVIEW", "RESOLVED", "REJECTED", "CLOSED"
}
