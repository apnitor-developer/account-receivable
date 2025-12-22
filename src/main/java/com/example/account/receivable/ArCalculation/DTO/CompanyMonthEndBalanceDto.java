package com.example.account.receivable.ArCalculation.DTO;

import java.math.BigDecimal;
import java.time.YearMonth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompanyMonthEndBalanceDto {
    private Long companyId;
    private YearMonth month;
    private BigDecimal monthEndBalance;
}
