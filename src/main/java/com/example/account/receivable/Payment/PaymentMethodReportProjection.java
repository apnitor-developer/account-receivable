package com.example.account.receivable.Payment;

import com.example.account.receivable.Payment.Enum.PaymentMethod;

public interface PaymentMethodReportProjection {
    PaymentMethod getPaymentMethod();
    Long getCount();
}
