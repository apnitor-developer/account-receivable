package com.example.account.receivable.Invoice.utils;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class InvoiceHeaderMapper {

    private final Map<String, String> canonicalToActual;

    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("customeremail", "customerEmail"),
            Map.entry("isgenerated", "isGenerated"),
            Map.entry("invoicenumber", "invoiceNumber"),
            Map.entry("invoicedate", "invoiceDate"),
            Map.entry("duedate", "dueDate"),
            Map.entry("itemname", "itemName"),
            Map.entry("quantity", "quantity"),
            Map.entry("qty", "quantity"),
            Map.entry("rate", "rate"),
            Map.entry("tax", "tax"),
            Map.entry("note", "note")
    );

    public InvoiceHeaderMapper(Set<String> headers) {
        this.canonicalToActual = headers.stream()
                .collect(Collectors.toMap(
                        this::normalize,
                        h -> h,
                        (a, b) -> a
                ));
    }

    private String normalize(String s) {
        String n = s.toLowerCase()
                .replace(" ", "")
                .replace("_", "")
                .replace("-", "");
        return ALIASES.getOrDefault(n, n);
    }

    public String get(ImportRow row, String canonical) {
        String header = canonicalToActual.get(canonical);
        if (header == null) return null;

        String val = row.get(header);
        return (val == null || val.isBlank()) ? null : val.trim();
    }
}
