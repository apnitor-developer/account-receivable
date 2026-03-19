package com.example.account.receivable.ArCalculation.DTO;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyMonthEndBalanceFullDto {

    private Long id;
    private Long companyId;
    private String yearMonth;
    private LocalDate asOfDate;
    private BigDecimal balance;
    private LocalDateTime calculatedAt;
    private Boolean locked;
}
