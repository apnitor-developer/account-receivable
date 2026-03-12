package com.example.account.receivable.Common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Invoice.Entity.Invoice;

@Service
@RequiredArgsConstructor
public class InvoiceTemplateService {

    private final TemplateEngine templateEngine;


    // PDF ONLY
    public String generatePdfHtml(Invoice invoice, Company company) {
        Context ctx = new Context();
        ctx.setVariable("invoice", invoice);
        ctx.setVariable("company", company);
        ctx.setVariable("customer", invoice.getCustomer());

        return templateEngine.process("invoice-pdf-template", ctx);
    }

    // EMAIL ONLY
    public String generateEmailHtml(Invoice invoice, Company company) {
        Context ctx = new Context();
        ctx.setVariable("invoice", invoice);
        ctx.setVariable("company", company);
        ctx.setVariable("customer", invoice.getCustomer());

        return templateEngine.process("invoice-email-template", ctx);
    }

    public String generateHtmlReminder(Invoice invoice , Company company , int level) {
        Context ctx = new Context();

        ctx.setVariable("invoice", invoice);
        ctx.setVariable("customer", invoice.getCustomer());
        ctx.setVariable("company", company);
        ctx.setVariable("level", level);

        return templateEngine.process("invoice-reminder-template", ctx);
    }
}