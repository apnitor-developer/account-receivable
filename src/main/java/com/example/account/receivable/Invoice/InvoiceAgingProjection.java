package com.example.account.receivable.Invoice;

public interface InvoiceAgingProjection {
    Long getCurrent();
    Long getDays0to30();
    Long getDays31to60();
    Long getDays61to90();
    Long getDays90Plus();
}
