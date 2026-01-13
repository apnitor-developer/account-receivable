package com.example.account.receivable.BankReconciliation.Utils;

import java.util.HashMap;
import java.util.Map;

public class BaiCodeUtil {

    private static final Map<String, BaiTransactionInfo> BAI_CODE_MAP =
            new HashMap<>();

    static {

        // ACH
        register("142", "ACH Credit Receipt", "CREDIT");
        register("165", "ACH Debit Collection", "CREDIT");
        register("257", "ACH Credit Payment Return", "CREDIT");
        register("261", "ACH Credit Reject", "CREDIT");
        register("447", "ACH Credit Payment", "DEBIT");
        register("451", "ACH Debit Payment", "DEBIT");
        register("557", "ACH Credit Receipt Return", "DEBIT");
        register("561", "ACH Debit Reject", "DEBIT");

        // Wire
        // register("195", "Incoming Wire", "CREDIT");
        register("495", "Outgoing Wire", "DEBIT");
        register("266", "Outgoing Wire Return", "CREDIT");

        // Fees & Interest
        register("698", "Fee Payment", "DEBIT");
        register("354", "Interest Adjustment", "CREDIT");
        register("654", "Overdraft Interest", "DEBIT");

        // Checks
        register("475", "Check Paid", "DEBIT");
        register("175", "Check Deposited", "CREDIT");
        register("255", "Check Returned", "CREDIT");

        // FPS / RTP / SEPA / BACS / CHAPS (sample)
        register("195", "FPS Incoming", "CREDIT");
        // register("447", "SEPA Credit Payment", "DEBIT");
        // register("195", "RTP Incoming", "CREDIT");
        // register("495", "Outgoing CHAPS", "DEBIT");
    }

    private static void register(String code, String type, String dc) {
        BAI_CODE_MAP.put(code, new BaiTransactionInfo(code, type, dc));
    }

    public static BaiTransactionInfo getInfo(String code) {
        return BAI_CODE_MAP.getOrDefault(
                code,
                new BaiTransactionInfo(code, "UNKNOWN", "UNKNOWN")
        );
    }
}

