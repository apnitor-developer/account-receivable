package com.example.account.receivable.ArCodes;

public enum ArCodeType {

    // Financial setup
    BANK_CASH,
    GL,

    // Adjustments & disputes
    ADJUST_REASON,
    DISPUTE,
    MEMO,

    // Customer & sales classification
    CUSTOMER_CLASS,
    SALES_REP,
    TERRITORY,
    CATEGORY,

    // Collections & aging
    AGING,
    DUNNING,
    PROMISE_TO_PAY,

    // Control & workflow
    HOLD,
    CYCLE,
    OPERATOR
}