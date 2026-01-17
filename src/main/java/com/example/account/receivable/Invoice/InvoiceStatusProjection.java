package com.example.account.receivable.Invoice;

public interface InvoiceStatusProjection {
    Long getOpen();
    Long getPartial();
    Long getPaid();
    Long getWrittenOff();
}
