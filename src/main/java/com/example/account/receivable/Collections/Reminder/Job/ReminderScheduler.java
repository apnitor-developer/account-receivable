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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final InvoiceRepository invoiceRepository;
    private final ReminderService reminderService;

    @Scheduled(cron = "0 01 8 * * ?", zone = "Asia/Kolkata")
    public void runAutomaticReminders() {

        List<Invoice> invoices =
                invoiceRepository.findByStatusIn(
                        List.of(InvoiceStatus.OPEN, InvoiceStatus.PARTIAL)
                );

        for (Invoice invoice : invoices) {

            try {

                if (invoice.getBalanceDue().compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                reminderService.processAutomaticReminder(invoice);

                Thread.sleep(2000); // wait 2 seconds to avoid SMTP rate limit

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt(); // restore interrupt flag
                log.error("Scheduler interrupted", e);

            } catch (Exception e) {

                log.error("Failed to process reminder for invoice {}", invoice.getId(), e);

            }
        }
    }
}
