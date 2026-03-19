package com.example.account.receivable.ArCalculation.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.account.receivable.ArCalculation.DTO.CompanyMonthEndBalanceDto;
import com.example.account.receivable.ArCalculation.DTO.CompanyMonthEndBalanceFullDto;
import com.example.account.receivable.ArCalculation.DTO.MonthEndRequest;
import com.example.account.receivable.ArCalculation.Entity.CompanyMonthEndBalance;
import com.example.account.receivable.ArCalculation.Repository.CompanyMonthEndBalanceRepository;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyBalanceCalculator {

    private final InvoiceRepository invoiceRepository;
    private final CompanyMonthEndBalanceRepository repository;

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


    public List<CompanyMonthEndBalanceFullDto> getYearlyBalances(MonthEndRequest request) {

        List<CompanyMonthEndBalance> entities =
                repository.findByCompanyIdAndYear(request.getCompanyId(), String.valueOf(
                        request.getYear()
                ));

        return entities.stream()
                .map(e -> CompanyMonthEndBalanceFullDto.builder()
                        .id(e.getId())
                        .companyId(e.getCompanyId())
                        .yearMonth(e.getYearMonth())
                        .asOfDate(e.getAsOfDate())
                        .balance(e.getBalance())
                        .calculatedAt(e.getCalculatedAt())
                        .locked(e.getLocked())
                        .build()
                )
                .toList();
        }
}
