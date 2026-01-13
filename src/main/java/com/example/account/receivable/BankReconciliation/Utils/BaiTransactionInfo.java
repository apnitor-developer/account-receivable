package com.example.account.receivable.BankReconciliation.Utils;

import lombok.Data;

@Data
public class BaiTransactionInfo {

    private final String code;
    private final String transactionType;
    private final String debitCredit;
}

