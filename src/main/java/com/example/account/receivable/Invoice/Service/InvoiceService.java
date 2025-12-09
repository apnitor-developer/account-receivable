package com.example.account.receivable.Invoice.Service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Common.EmailService;
import com.example.account.receivable.Common.InvoiceTemplateService;
import com.example.account.receivable.Common.PdfGeneratorService;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Repository.CustomerRepository;
import com.example.account.receivable.Invoice.Dto.InvoiceDto;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;
import com.example.account.receivable.ProductAndService.Repository.ProductAndServiceRepository;

import jakarta.transaction.Transactional;

@Service
public class InvoiceService {
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;

    private static final String INVOICE_PREFIX = "INV-";
    private static final int INVOICE_NUMBER_WIDTH = 4;  // 0001 – 9999
    private final EmailService emailService;
    private final InvoiceTemplateService invoiceTemplateService;
    private final PdfGeneratorService pdfGeneratorService;

    public InvoiceService(
            CustomerRepository customerRepository,
            InvoiceRepository invoiceRepository,
            ProductAndServiceRepository productRepository,
            EmailService emailService,
            InvoiceTemplateService invoiceTemplateService,
            PdfGeneratorService pdfGeneratorService
    ) {
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
        this.emailService = emailService;
        this.invoiceTemplateService = invoiceTemplateService;
        this.pdfGeneratorService = pdfGeneratorService;
    }

    
    public void sendInvoiceEmail(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));

        String html = invoiceTemplateService.generateHtml(invoice);
        System.out.println( "Invoice html" + html);

        byte[] pdf = pdfGeneratorService.generatePdf(html);
        System.out.println( "Invoice pdf" + pdf);

        String customerEmail = invoice.getCustomer().getEmail();
        System.out.println("Customer email" + customerEmail);
        
        String subject = "Invoice " + invoice.getInvoiceNumber();
        System.out.println( "Subject" + subject);

        emailService.sendWithAttachment(customerEmail, subject, html, pdf);

    }


    //Get Customer Open Invoices
    public List<Invoice> getOpenInvoices(Long customerId){

        Customer customer = customerRepository.findById(customerId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        // Fetch only unpaid invoices
        List<String> statuses = List.of("OPEN", "PARTIAL");

        return invoiceRepository.findByCustomerIdAndStatusIn(customerId, statuses);
    }




    @Transactional
    public Invoice createInvoice(Long customerId, InvoiceDto dto) {
        // Validate Customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        // --- Resolve invoice number ---
        boolean generatedFlag = Boolean.TRUE.equals(dto.getGenerated());

        String invoiceNumber;

        boolean auto = Boolean.TRUE.equals(dto.getGenerated());
        if (auto) {
            invoiceNumber = generateUniqueInvoiceNumber();
        } else {
            if (dto.getInvoiceNumber() == null || dto.getInvoiceNumber().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invoice number is required when isGenerated is false"
                );
            }
            String manual = dto.getInvoiceNumber().trim();
            if (invoiceRepository.existsByInvoiceNumber(manual)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Invoice number already exists"
                );
            }
            invoiceNumber = manual;
        }

        // 3. Validate and use rate & tax
        if (dto.getRate() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Rate is required"
            );
        }

        BigDecimal rate = dto.getRate();

        // Tax can be required or optional depending on your rule
        BigDecimal taxAmount = dto.getTaxAmount() != null
                ? dto.getTaxAmount()
                : BigDecimal.ZERO;

        // Total = rate + tax
        BigDecimal totalAmount = rate.add(taxAmount);

        // 4. Build invoice
        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .invoiceDate(dto.getInvoiceDate())
                .dueDate(dto.getDueDate())
                .description(dto.getDescription())
                .note(dto.getNote())
                .subTotal(rate)          // treat subTotal as the "rate"
                .taxAmount(taxAmount)    // tax from dto
                .totalAmount(totalAmount)
                .balanceDue(totalAmount)
                .customer(customer)
                .deleted(false)
                .active(true)
                .generated(generatedFlag)
                .build();


        try {
            return invoiceRepository.save(invoice);
        } catch (DataIntegrityViolationException ex) {
            // Extra safety if two requests race for same number
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invoice number already exists, please try again"
            );
        }
    }



    private String generateUniqueInvoiceNumber() {
        String prefix = INVOICE_PREFIX;

        // Get the last invoice number starting with "INV-"
        var lastOpt = invoiceRepository
                .findTopByInvoiceNumberStartingWithOrderByInvoiceNumberDesc(prefix);

        int nextNumber = 1; // default if none exist

        if (lastOpt.isPresent()) {
            String lastNumber = lastOpt.get().getInvoiceNumber(); // e.g. "INV-0042"
            String[] parts = lastNumber.split("-");
            if (parts.length == 2) {
                try {
                    int current = Integer.parseInt(parts[1]);
                    if (current >= 9999) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Maximum invoice number (INV-9999) reached"
                        );
                    }
                    nextNumber = current + 1;
                } catch (NumberFormatException ignore) {
                    // If previous value is malformed, just fall back to 1
                    nextNumber = 1;
                }
            }
        }

        // Format as 4-digit number with leading zeros
        String formatted = String.format("%0" + INVOICE_NUMBER_WIDTH + "d", nextNumber);
        String candidate = prefix + formatted; // e.g. "INV-0007"

        // Double-check uniqueness in case of manual numbers or race conditions
        int safetyCounter = 0;
        while (invoiceRepository.existsByInvoiceNumber(candidate)) {
            safetyCounter++;
            if (safetyCounter > 20) {
                // Avoid infinite loop if something weird is happening
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Unable to generate unique invoice number"
                );
            }

            nextNumber++;
            if (nextNumber > 9999) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Maximum invoice number (INV-9999) reached"
                );
            }

            formatted = String.format("%0" + INVOICE_NUMBER_WIDTH + "d", nextNumber);
            candidate = prefix + formatted;
        }

        return candidate;
    }

    // Get all Invoices
    public Page<Invoice> getAllInvoices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return invoiceRepository.findByDeletedFalse(pageable);
    }

    // Get Single customer invoice
    public List<Invoice> getSingleCustomerInvoice(Long customerId) {
        return invoiceRepository.findByCustomerIdAndDeletedFalse(customerId);
    }

    // Invoice By Id
    public Invoice getInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found with this Id"));
        return invoice;
    }
}