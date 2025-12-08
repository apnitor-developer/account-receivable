package com.example.account.receivable.Customer.Dto.CustomerDTO;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerCsv {

    private int totalRows;
    private int successCount;
    private int failureCount;
    private List<RowError> errors;

    @Data
    @Builder
    public static class RowError {
        private long rowNumber;
        private String message;
    }
}
