package com.example.account.receivable.ArCalculation.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;

import org.springframework.stereotype.Service;

import com.example.account.receivable.ArCalculation.DTO.CompanyMonthEndBalanceDto;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyBalanceCalculator {

    private final InvoiceRepository invoiceRepository;

    public CompanyMonthEndBalanceDto calculate(
            Long companyId,
            YearMonth month
    ) {
        LocalDate asOfDate =
        LocalDate.now()
                 .minusMonths(1)
                 .with(TemporalAdjusters.lastDayOfMonth());

        BigDecimal balance =
                invoiceRepository.getCompanyMonthEndBalance(
                        companyId,
                        asOfDate
                );

        return new CompanyMonthEndBalanceDto(
                companyId,
                month,
                balance
        );
    }
}
