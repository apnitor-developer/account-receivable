package com.example.account.receivable.ArCalculation.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.stereotype.Service;

import com.example.account.receivable.ArCalculation.DTO.CustomerMonthEndBalanceDto;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class CustomerBalanceCalculator {
    
    private final InvoiceRepository invoiceRepository;

    public CustomerMonthEndBalanceDto calculate(
            Long customerId,
            YearMonth month
    ) {
        LocalDate monthEnd = month.atEndOfMonth();

        BigDecimal balance =
                invoiceRepository.getCustomerMonthEndBalance(
                        customerId,
                        monthEnd
                );

        return new CustomerMonthEndBalanceDto(
                customerId,
                month,
                balance
        );
    }
}
