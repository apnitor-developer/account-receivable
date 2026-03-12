package com.example.account.receivable.Collections.Reminder.Job;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.account.receivable.Collections.Reminder.Service.ReminderService;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Enum.InvoiceStatus;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final InvoiceRepository invoiceRepository;
    private final ReminderService reminderService;

    @Scheduled(cron = "0 21 11 * * ?") // every day 10:35 AM
    public void runAutomaticReminders() {

        List<Invoice> invoices =
        invoiceRepository.findByStatusIn(
                List.of(InvoiceStatus.OPEN, InvoiceStatus.PARTIAL)
        );

        for (Invoice invoice : invoices) {

            if (invoice.getBalanceDue().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            reminderService.processAutomaticReminder(invoice);
        }
    }
}
