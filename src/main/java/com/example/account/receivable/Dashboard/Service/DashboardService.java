package com.example.account.receivable.Dashboard.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.account.receivable.ArCalculation.DTO.CompanyBalanceSeriesDto;
import com.example.account.receivable.ArCalculation.Entity.CompanyMonthEndBalance;
import com.example.account.receivable.ArCalculation.Repository.CompanyMonthEndBalanceRepository;
import com.example.account.receivable.Collections.PromiseToPay.Entity.PromiseStatus;
import com.example.account.receivable.Collections.PromiseToPay.Repository.PromiseToPayRepo;
import com.example.account.receivable.Customer.Repository.CustomerRepository;
import com.example.account.receivable.Dashboard.DTO.DashboardSummaryResponse;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;
import com.example.account.receivable.Payment.Repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PromiseToPayRepo promiseToPayRepo;
    private final CompanyMonthEndBalanceRepository companyMonthEndBalanceRepository;

    public DashboardSummaryResponse getDashboardSummary(Long companyId){

        
        LocalDate today = LocalDate.now();

        // Payments:
        //(total Payment)
        BigDecimal totalPayments = paymentRepository.getTotalPaymentsByCompany(companyId);

        //(today Payment)
        BigDecimal todayPayments = paymentRepository.getTodayPaymentsByCompany(companyId , today);


        //Total Customers
        long totalCustomers = customerRepository.countByCompanyAndDeletedFalse(companyId);


        // Receivables
        BigDecimal totalReceivables = invoiceRepository.getTotalReceivablesByCompany(companyId);

        BigDecimal currentReceivables = invoiceRepository.getCurrentReceivablesByCompany(companyId ,  today);


        // Counts
        long totalInvoices = invoiceRepository.countByCompanyAndDeletedFalse(companyId);

        long pendingInvoices = invoiceRepository.countPendingByCompany(companyId);


        // Current Promise to Pay
        BigDecimal currentPromiseToPay =
                promiseToPayRepo.getCurrentPromiseAmountByCompany(
                        companyId,
                        List.of(PromiseStatus.PENDING, PromiseStatus.DUE_TODAY),
                        today
                );

        return new DashboardSummaryResponse(
            totalPayments,
            todayPayments,
            totalCustomers,
            totalReceivables,
            currentReceivables,
            totalInvoices,
            pendingInvoices,
            currentPromiseToPay
        );
    }


    //Company Previous monthly balances
    public CompanyBalanceSeriesDto getCompanyBalanceSeries(Long companyId, int months) {
        if (months <= 0) months = 12;

        YearMonth now = YearMonth.now();
        YearMonth start = now.minusMonths(months - 1);

        String startYm = start.toString();
        String endYm = now.toString();

        List<CompanyMonthEndBalance> rows =
                companyMonthEndBalanceRepository
                        .findByCompanyIdAndYearMonthBetweenOrderByYearMonthAsc(companyId, startYm, endYm);

        List<CompanyBalanceSeriesDto.Point> points = rows.stream()
                .map(r -> new CompanyBalanceSeriesDto.Point(r.getYearMonth(), r.getBalance()))
                .toList();

        return new CompanyBalanceSeriesDto(companyId, points);
    }
}
