package com.example.account.receivable.Collections.Reminder.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Common.EmailService;
import com.example.account.receivable.Common.InvoiceTemplateService;
import com.example.account.receivable.Common.PdfGeneratorService;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.Customer.Entity.CompanyCustomers;
import com.example.account.receivable.Customer.Entity.CustomerDunningCreditSettings;
import com.example.account.receivable.Customer.Repository.CompanyCustomerRepository;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceTemplateService invoiceTemplateService;
    private final PdfGeneratorService pdfGeneratorService;
    private final EmailService emailService;
    private final CompanyRepository companyRepository;
    private final CompanyCustomerRepository companyCustomerRepository;

    public void sendInvoiceReminder(Long invoiceId, Long companyId, int level) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Invoice not found"
            ));

        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Company not found"
            ));

        if (invoice.isDeleted()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invoice is deleted"
            );
        }

        if (invoice.getBalanceDue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invoice has no pending balance"
            );
        }

        if (invoice.getDueDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invoice is not overdue"
            );
        }

        String html = invoiceTemplateService.generateHtmlReminder(invoice, company, level);

        byte[] pdf = pdfGeneratorService.generatePdf(html);

        String customerEmail = invoice.getCustomer().getEmail();

        String subject = "Payment Reminder (Level " + level + ") - Invoice " + invoice.getInvoiceNumber();

        emailService.sendWithAttachment(
            customerEmail,
            subject,
            html,
            pdf
        );
    }


    private int determineLevel(CustomerDunningCreditSettings dunning, long daysPastDue) {

        int level1 = Integer.parseInt(dunning.getLevel1());
        int level2 = Integer.parseInt(dunning.getLevel2());
        int level3 = Integer.parseInt(dunning.getLevel3());
        int level4 = Integer.parseInt(dunning.getLevel4());

        if (daysPastDue >= level4) return 4;
        if (daysPastDue >= level3) return 3;
        if (daysPastDue >= level2) return 2;
        if (daysPastDue >= level1) return 1;

        return 0;
    }


    public void processAutomaticReminder(Invoice invoice) {

        CustomerDunningCreditSettings dunning = invoice.getCustomer().getDunning();

        if (dunning == null) {
            
            return; // customer has no dunning settings
        }


        long daysPastDue = ChronoUnit.DAYS.between(
            invoice.getDueDate(),
            LocalDate.now()
        );

        int level = determineLevel(dunning, daysPastDue);

        if (level == 0) {
            return;
        }

        if (level <= invoice.getLastDunningLevelSent()) {
            return;
        }

        CompanyCustomers companyCustomer = companyCustomerRepository.findFirstByCustomer_Id(invoice.getCustomer().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Company not found for customer"
                ));

        Long companyId = companyCustomer.getCompany().getId();

        sendInvoiceReminder(invoice.getId(), companyId, level);

        invoice.setLastDunningLevelSent(level);
        invoiceRepository.save(invoice);
    }
}

