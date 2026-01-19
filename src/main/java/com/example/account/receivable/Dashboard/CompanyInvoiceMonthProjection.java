package com.example.account.receivable.Dashboard;

import java.math.BigDecimal;

public interface CompanyInvoiceMonthProjection {
    Integer getYear();          // 2026
    Integer getMonth();         // 1..12
    Long getInvoiceCount();
    BigDecimal getTotalAmount();
}
