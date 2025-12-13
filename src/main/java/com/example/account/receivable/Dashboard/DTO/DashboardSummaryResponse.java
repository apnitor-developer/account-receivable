package com.example.account.receivable.Dashboard.DTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryResponse {

    private BigDecimal totalPaymentReceived;
    private BigDecimal todayPaymentReceived;

    private long totalCustomers;

    private BigDecimal totalReceivables;
    private BigDecimal currentReceivables;

    private long totalInvoices;
    private long pendingInvoices;

    private BigDecimal currentPromiseToPay;
}
