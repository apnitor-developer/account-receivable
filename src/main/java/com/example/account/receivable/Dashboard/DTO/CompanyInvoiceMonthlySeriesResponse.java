package com.example.account.receivable.Dashboard.DTO;

import java.math.BigDecimal;
import java.util.List;

public record CompanyInvoiceMonthlySeriesResponse(
        Long companyId,
        List<Point> points
) {
    public record Point(
            String yearMonth,     // "2026-01"
            long invoiceCount,
            BigDecimal totalAmount
    ) {}
}
