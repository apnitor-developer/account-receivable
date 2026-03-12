package com.example.account.receivable.LateFee.Job;

import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Enum.InvoiceStatus;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;
import com.example.account.receivable.LateFee.Service.LateFeeProcessingService;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LateFeeScheduler {

    private final InvoiceRepository invoiceRepository;
    private final LateFeeProcessingService lateFeeProcessingService;

    /**
     * Runs every day at 1 PM IST
     */
    @Scheduled(cron = "0 30 18 * * *")
    public void applyLateFees() {

        LocalDate today = LocalDate.now();

        List<Invoice> overdueInvoices =
                invoiceRepository.findByDueDateBeforeAndStatusIn(
                        today,
                        List.of(
                                InvoiceStatus.OPEN,
                                InvoiceStatus.PARTIAL
                        )
                );

        for (Invoice invoice : overdueInvoices) {

            try {

                lateFeeProcessingService.processLateFee(invoice);

            } catch (Exception ex) {

                System.err.println(
                        "Late fee processing failed for invoice "
                                + invoice.getId()
                );

                ex.printStackTrace();
            }
        }
    }
}
