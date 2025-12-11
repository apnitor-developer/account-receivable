package com.example.account.receivable.Aging.Service;

import com.example.account.receivable.Aging.DTO.AgingReportResponse;
import com.example.account.receivable.Aging.DTO.CustomerAgingDto;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AgingReportService {

    private final InvoiceRepository invoiceRepository;

    public AgingReportResponse getAgingReport(LocalDate asOfDate, Long customerId, String status) {

        if (asOfDate == null) {
            asOfDate = LocalDate.now();
        }

        // Get all invoices with outstanding balance
        List<Invoice> allOpenInvoices = invoiceRepository.findByActiveTrueAndDeletedFalseAndBalanceDueGreaterThan(BigDecimal.ZERO);

        // Apply optional filters in Java
        List<Invoice> filtered = new ArrayList<>();
        for (Invoice inv : allOpenInvoices) {

            // filter by customer
            if (customerId != null) {
                Customer c = inv.getCustomer();
                if (c == null || !Objects.equals(c.getId(), customerId)) {
                    continue;
                }
            }

            // filter by status (OPEN, PARTIAL, etc.) – case insensitive
            if (status != null && !status.isBlank()) {
                String invStatus = inv.getStatus();
                if (invStatus == null || !invStatus.equalsIgnoreCase(status)) {
                    continue;
                }
            }

            filtered.add(inv);
        }

        // 3. Group by customer and calculate buckets
        Map<Long, CustomerAgingDto> byCustomer = new LinkedHashMap<>();

        for (Invoice invoice : filtered) {
            Customer customer = invoice.getCustomer();
            if (customer == null) continue;

            Long cid = customer.getId();
            String cname = customer.getCustomerName();

            // Get or create row for this customer
            CustomerAgingDto row = byCustomer.computeIfAbsent(cid, id -> {
                CustomerAgingDto dto = new CustomerAgingDto();
                dto.setCustomerId(cid);
                dto.setCustomerName(cname);
                dto.setTotalDue(BigDecimal.ZERO);
                dto.setCurrent(BigDecimal.ZERO);
                dto.setBucket1To30(BigDecimal.ZERO);
                dto.setBucket31To60(BigDecimal.ZERO);
                dto.setBucket61To90(BigDecimal.ZERO);
                dto.setBucketGt90(BigDecimal.ZERO);
                return dto;
            });

            BigDecimal balance = invoice.getBalanceDue();
            if (balance == null || balance.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // Add to total due
            row.setTotalDue(row.getTotalDue().add(balance));

            // Days overdue
            LocalDate dueDate = invoice.getDueDate();
            long daysOverdue = 0;
            if (dueDate != null) {
                daysOverdue = ChronoUnit.DAYS.between(dueDate, asOfDate);
            }

            // 4. Put into exact bucket
            if (daysOverdue <= 0) {
                // Not overdue yet
                row.setCurrent(row.getCurrent().add(balance));
            } else if (daysOverdue >= 1 && daysOverdue <= 30) {
                row.setBucket1To30(row.getBucket1To30().add(balance));
            } else if (daysOverdue >= 31 && daysOverdue <= 60) {
                row.setBucket31To60(row.getBucket31To60().add(balance));
            } else if (daysOverdue >= 61 && daysOverdue <= 90) {
                row.setBucket61To90(row.getBucket61To90().add(balance));
            } else { // > 90
                row.setBucketGt90(row.getBucketGt90().add(balance));
            }
        }

        List<CustomerAgingDto> rows = new ArrayList<>(byCustomer.values());

        return new AgingReportResponse(asOfDate, rows);
    }
}