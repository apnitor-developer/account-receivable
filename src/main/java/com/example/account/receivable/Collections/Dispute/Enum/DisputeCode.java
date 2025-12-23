package com.example.account.receivable.Collections.Dispute.Enum;

import lombok.Getter;

@Getter
public enum DisputeCode {

    PRICE("Pricing Issue"),
    TAX("Tax Issue"),
    DUPLICATE_INV("Duplicate Invoice"),
    WRONG_INV("Wrong Invoice");

    private final String label;

    public String getCode() {
    return name();
    }

    DisputeCode(String label) {
        this.label = label;
    }
}

