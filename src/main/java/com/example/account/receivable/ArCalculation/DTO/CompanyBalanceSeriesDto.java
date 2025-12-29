package com.example.account.receivable.ArCalculation.DTO;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyBalanceSeriesDto {

    private Long companyId;
    private List<Point> series;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Point {
        private String month;       // "YYYY-MM"
        private BigDecimal balance; // stored snapshot
    }
}
