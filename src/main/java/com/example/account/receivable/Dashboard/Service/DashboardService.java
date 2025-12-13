package com.example.account.receivable.Dashboard.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

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

    public DashboardSummaryResponse getDashboardSummary(){

        
        LocalDate today = LocalDate.now();

        // Payments:
        //(total Payment)
        BigDecimal totalPayments = paymentRepository.getTotalPayments();

        //(today Payment)
        BigDecimal todayPayments = paymentRepository.getTodayPayments(today);


        //Total Customers
        long totalCustomers = customerRepository.countByDeletedFalse();


        // Receivables
        BigDecimal totalReceivables = invoiceRepository.getTotalReceivables();

        BigDecimal currentReceivables = invoiceRepository.getCurrentReceivables(today);


        // Counts
        long totalInvoices = invoiceRepository.countByDeletedFalse();

        long pendingInvoices = invoiceRepository.countByBalanceDueGreaterThan(BigDecimal.ZERO);


        // Current Promise to Pay
        BigDecimal currentPromiseToPay =
                promiseToPayRepo.getCurrentPromiseAmount(
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
}
