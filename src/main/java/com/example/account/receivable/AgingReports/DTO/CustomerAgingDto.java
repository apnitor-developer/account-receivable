package com.example.account.receivable.AgingReports.DTO;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAgingDto {
    
    private Long customerId;
    private String customerName;

    private BigDecimal totalDue;
    private BigDecimal current;
    private Map<String, BigDecimal> buckets = new LinkedHashMap<>();
}
