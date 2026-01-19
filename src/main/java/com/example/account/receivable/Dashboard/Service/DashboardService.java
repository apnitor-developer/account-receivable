package com.example.account.receivable.Dashboard.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.account.receivable.ArCalculation.DTO.CompanyBalanceSeriesDto;
import com.example.account.receivable.ArCalculation.Entity.CompanyMonthEndBalance;
import com.example.account.receivable.ArCalculation.Repository.CompanyMonthEndBalanceRepository;
import com.example.account.receivable.Collections.PromiseToPay.Entity.PromiseStatus;
import com.example.account.receivable.Collections.PromiseToPay.Repository.PromiseToPayRepo;
import com.example.account.receivable.Customer.Repository.CustomerRepository;
import com.example.account.receivable.Dashboard.CompanyInvoiceMonthProjection;
import com.example.account.receivable.Dashboard.DTO.CompanyInvoiceMonthlySeriesResponse;
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


    //Calculate invoice monthly amount
    public CompanyInvoiceMonthlySeriesResponse getCompanyInvoiceMonthlySeries(
            Long companyId,
            Integer year
    ) {
        YearMonth start;
        YearMonth end;

        if (year != null) {
            start = YearMonth.of(year, 1);
            YearMonth now = YearMonth.now();
            end = (year == now.getYear()) ? now : YearMonth.of(year, 12);
        } else {
            end = YearMonth.now();
            start = end.minusMonths(11);
        }

        LocalDate fromDate = start.atDay(1);
        LocalDate toDate = end.atEndOfMonth();

        List<CompanyInvoiceMonthProjection> rows =
                invoiceRepository.getCompanyInvoiceMonthlyTotals(companyId, fromDate, toDate);

        // Map: YYYY-MM -> projection
        Map<String, CompanyInvoiceMonthProjection> byMonth = new HashMap<>();
        for (CompanyInvoiceMonthProjection r : rows) {
            String key = String.format("%04d-%02d", r.getYear(), r.getMonth());
            byMonth.put(key, r);
        }

        List<CompanyInvoiceMonthlySeriesResponse.Point> points = new ArrayList<>();

        YearMonth cursor = start;
        while (!cursor.isAfter(end)) {
            String key = cursor.toString(); // YYYY-MM

            CompanyInvoiceMonthProjection r = byMonth.get(key);

            long count = (r == null) ? 0L : r.getInvoiceCount();
            BigDecimal total = (r == null) ? BigDecimal.ZERO : r.getTotalAmount();

            points.add(new CompanyInvoiceMonthlySeriesResponse.Point(key, count, total));

            cursor = cursor.plusMonths(1);
        }

        return new CompanyInvoiceMonthlySeriesResponse(companyId, points);
    }



}
