package com.example.account.receivable.LateFee.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Common.EmailService;
import com.example.account.receivable.Common.InvoiceTemplateService;
import com.example.account.receivable.Common.PdfGeneratorService;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Entity.InvoiceItem;
import com.example.account.receivable.Invoice.Enum.InvoiceStatus;
import com.example.account.receivable.Invoice.Enum.InvoiceType;
import com.example.account.receivable.Invoice.Repository.InvoiceItemRepo;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;
import com.example.account.receivable.Invoice.Service.InvoiceService;
import com.example.account.receivable.LateFee.Entity.LateFeeRule;
import com.example.account.receivable.LateFee.Repository.LateFeeRuleRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LateFeeProcessingService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepo invoiceItemRepo;
    private final LateFeeRuleRepository lateFeeRuleRepository;
    private final InvoiceService invoiceService;
    private final InvoiceTemplateService invoiceTemplateService;
    private final PdfGeneratorService pdfGeneratorService;
    private final EmailService emailService;
    private final CompanyRepository companyRepository;

    @Transactional
    public void processLateFee(Invoice invoice) {

        if (invoice.getStatus() != InvoiceStatus.OPEN &&
            invoice.getStatus() != InvoiceStatus.PARTIAL) {
            return;
        }

        Company company = companyRepository
            .findCompanyByCustomerId(invoice.getCustomer().getId())
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND , "Company not found for customer"));

        LateFeeRule rule =
                lateFeeRuleRepository.findByCompanyId(company.getId())
                .orElse(null);

        if (rule == null) return;

        long daysPastDue =
                ChronoUnit.DAYS.between(invoice.getDueDate(), LocalDate.now());

        if (daysPastDue < rule.getGracePeriodDays()) return;

        if (invoice.getStatus() == InvoiceStatus.DISCONTINUED) return;

        BigDecimal originalAmount = invoice.getBalanceDue();

        BigDecimal lateFee =
                originalAmount
                        .multiply(rule.getLateFeePercentage())
                        .divide(BigDecimal.valueOf(100))
                        .add(rule.getMandatoryCharge());

        BigDecimal newTotal = originalAmount.add(lateFee);

        // discontinue original invoice
        invoice.setStatus(InvoiceStatus.DISCONTINUED);
        invoiceRepository.save(invoice);

        // generate invoice number using existing logic
        String newInvoiceNumber = invoiceService.generateUniqueInvoiceNumber();

        Invoice newInvoice = Invoice.builder()
                .invoiceNumber(newInvoiceNumber)
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(10))
                .customer(invoice.getCustomer())
                .parentInvoice(invoice)
                .invoiceType(InvoiceType.ORIGINAL)
                .lastDunningLevelSent(0)
                .status(InvoiceStatus.OPEN)
                .subTotal(newTotal)
                .totalAmount(newTotal)
                .balanceDue(newTotal)
                .generated(true)
                .build();

        newInvoice = invoiceRepository.save(newInvoice);

        // create invoice items
        InvoiceItem outstandingItem = InvoiceItem.builder()
                .itemName("Outstanding Balance")
                .description("Outstanding amount from invoice "
                        + invoice.getInvoiceNumber())
                .quantity(1)
                .rate(originalAmount)
                .amount(originalAmount)
                .taxAmount(BigDecimal.ZERO)
                .total(originalAmount)
                .invoice(newInvoice)
                .build();

        InvoiceItem lateFeeItem = InvoiceItem.builder()
                .itemName("Late Payment Fee")
                .description("Late payment charge after grace period")
                .quantity(1)
                .rate(lateFee)
                .amount(lateFee)
                .taxAmount(BigDecimal.ZERO)
                .total(lateFee)
                .invoice(newInvoice)
                .build();

        invoiceItemRepo.saveAll(List.of(outstandingItem, lateFeeItem));

        sendLateFeeEmail(newInvoice, company, originalAmount, lateFee);
    }

    private void sendLateFeeEmail(
            Invoice invoice,
            Company company,
            BigDecimal originalAmount,
            BigDecimal lateFee
    ) {

        String emailHtml =
                invoiceTemplateService.generateLateFeeEmailHtml(
                        invoice,
                        company,
                        originalAmount,
                        lateFee
                );

        String pdfHtml =
                invoiceTemplateService.generateLateFeePdfHtml(
                        invoice,
                        company,
                        originalAmount,
                        lateFee
                );

        byte[] pdf = pdfGeneratorService.generatePdf(pdfHtml);

        String subject =
                "Late Fee Applied to Invoice "
                        + invoice.getParentInvoice().getInvoiceNumber();

        emailService.sendWithAttachment(
                invoice.getCustomer().getEmail(),
                subject,
                emailHtml,
                pdf
        );
    }
}
