package com.example.account.receivable.Invoice.Job;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.account.receivable.Invoice.Entity.RecurringInvoiceTemplate;
import com.example.account.receivable.Invoice.Repository.RecurringInvoiceTemplateRepository;
import com.example.account.receivable.Invoice.Service.InvoiceService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RecurringInvoiceJob {

    private final RecurringInvoiceTemplateRepository templateRepo;
    private final InvoiceService invoiceService;

    @Scheduled(cron = "0 0 1 * * *")
    public void generateInvoices() {

        List<RecurringInvoiceTemplate> templates =
                templateRepo.findByActiveTrue();

        LocalDate today = LocalDate.now();

        for (RecurringInvoiceTemplate template : templates) {

            if (!template.getNextGenerationDate().isAfter(today)) {

                invoiceService.generateFromTemplate(template);
            }
        }
    }
}
