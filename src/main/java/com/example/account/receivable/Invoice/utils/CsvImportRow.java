package com.example.account.receivable.Invoice.utils;

import org.apache.commons.csv.CSVRecord;

public class CsvImportRow implements ImportRow {

    private final CSVRecord record;

    public CsvImportRow(CSVRecord record) {
        this.record = record;
    }

    @Override
    public String get(String header) {
        return record.isMapped(header) ? record.get(header) : null;
    }

    @Override
    public long getRowNumber() {
        return record.getRecordNumber();
    }
}

