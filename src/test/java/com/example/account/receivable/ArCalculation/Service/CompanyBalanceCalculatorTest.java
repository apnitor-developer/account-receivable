package com.example.account.receivable.ArCalculation.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.account.receivable.ArCalculation.DTO.CompanyMonthEndBalanceDto;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;

@ExtendWith(MockitoExtension.class)
class CompanyBalanceCalculatorTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private CompanyBalanceCalculator calculator;

    @Test
    void calculate_fetchesBalanceForMonthEnd() {
        YearMonth month = YearMonth.of(2024, 4);
        LocalDate asOf = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

        when(invoiceRepository.getCompanyMonthEndBalance(eq(9L), eq(asOf)))
                .thenReturn(new BigDecimal("1234.56"));

        CompanyMonthEndBalanceDto dto = calculator.calculate(9L, month);

        assertEquals(9L, dto.getCompanyId());
        assertEquals(month, dto.getMonth());
        assertEquals(new BigDecimal("1234.56"), dto.getMonthEndBalance());
    }
}
