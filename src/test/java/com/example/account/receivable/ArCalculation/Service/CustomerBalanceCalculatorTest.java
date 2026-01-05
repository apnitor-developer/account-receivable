package com.example.account.receivable.ArCalculation.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.account.receivable.ArCalculation.DTO.CustomerMonthEndBalanceDto;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;

@ExtendWith(MockitoExtension.class)
class CustomerBalanceCalculatorTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private CustomerBalanceCalculator calculator;

    @Test
    void calculate_usesRepositoryForGivenMonth() {
        YearMonth month = YearMonth.of(2024, 5);
        LocalDate asOf = month.atEndOfMonth();
        when(invoiceRepository.getCustomerMonthEndBalance(eq(44L), eq(asOf)))
                .thenReturn(new BigDecimal("789.10"));

        CustomerMonthEndBalanceDto dto = calculator.calculate(44L, month);

        assertEquals(44L, dto.getCustomerId());
        assertEquals(month, dto.getMonth());
        assertEquals(new BigDecimal("789.10"), dto.getMonthEndBalance());
    }
}
