package com.example.account.receivable.Collections.PromiseToPay.Entity;

public enum PromiseStatus {
    PENDING,       // Promise date is in future
    DUE_TODAY,     // Today == promise date
    BROKEN,        // Promise date passed, but payment not made
    COMPLETED      // Payment fully applied
}
