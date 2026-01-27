package com.example.account.receivable.Collections.Reminder.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Common.EmailService;
import com.example.account.receivable.Common.InvoiceTemplateService;
import com.example.account.receivable.Common.PdfGeneratorService;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Repository.CompanyRepository;
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

    public void sendInvoiceReminder(Long invoiceId , Long companyId) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Invoice not found"
                )
            );

            Company company = companyRepository.findById(companyId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        // 🔒 Business validations
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

        // 🧾 Generate email HTML (reuse existing template)
        String html = invoiceTemplateService.generateHtmlReminder(invoice , company);

        // 📄 Generate PDF
        byte[] pdf = pdfGeneratorService.generatePdf(html);

        // 📧 Email details
        String customerEmail = invoice.getCustomer().getEmail();
        String subject = "Payment Reminder – Invoice " + invoice.getInvoiceNumber();

        // 🚀 Send email
        emailService.sendWithAttachment(
            customerEmail,
            subject,
            html,
            pdf
        );
    }
}

