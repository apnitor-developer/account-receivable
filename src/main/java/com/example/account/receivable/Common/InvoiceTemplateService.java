package com.example.account.receivable.Common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.example.account.receivable.Invoice.Entity.Invoice;

@Service
@RequiredArgsConstructor
public class InvoiceTemplateService {

    private final TemplateEngine templateEngine;

    public String generateHtml(Invoice invoice) {
        Context ctx = new Context();

        ctx.setVariable("invoice", invoice);
        ctx.setVariable("customer", invoice.getCustomer());

        return templateEngine.process("invoice-template", ctx);
    }
}