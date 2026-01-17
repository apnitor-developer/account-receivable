package com.example.account.receivable.Payment;

import java.math.BigDecimal;

public interface MonthlyPaymentProjection {
    Integer getYear();
    Integer getMonth();
    BigDecimal getTotal();
}
