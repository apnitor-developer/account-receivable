package com.example.account.receivable.Dashboard;

import java.math.BigDecimal;

public interface CompanyInvoiceMonthProjection {
    String getYearMonth();      // "YYYY-MM"
    Long getInvoiceCount();
    BigDecimal getTotalAmount();
}
