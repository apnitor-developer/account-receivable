package com.example.account.receivable.Invoice.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Repository.CustomerRepository;
import com.example.account.receivable.Invoice.Dto.InvoiceDto;
import com.example.account.receivable.Invoice.Dto.InvoiceItemDto;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Entity.InvoiceItem;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;
import com.example.account.receivable.ProductAndService.Entity.ProductAndService;
import com.example.account.receivable.ProductAndService.Repository.ProductAndServiceRepository;

import jakarta.transaction.Transactional;

@Service
public class InvoiceService {
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final ProductAndServiceRepository productRepository;

    private static final String INVOICE_PREFIX = "INV-";
    private static final int INVOICE_NUMBER_WIDTH = 4;  // 0001 – 9999

    public InvoiceService(
            CustomerRepository customerRepository,
            InvoiceRepository invoiceRepository,
            ProductAndServiceRepository productRepository
    ) {
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
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

        // Create Invoice
        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)  
                .invoiceDate(dto.getInvoiceDate())
                .dueDate(dto.getDueDate())
                .note(dto.getNote())
                .customer(customer)
                .deleted(false)
                .active(true)
                .generated(generatedFlag)
                .build();

        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        List<InvoiceItem> invoiceItems = new ArrayList<>();

        // // Loop through Items
        // if (dto.getItems() != null) {
        //     for (InvoiceItemDto itemDto : dto.getItems()) {
        //         ProductAndService product = productRepository.findById(itemDto.getProductId())
        //                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        //         BigDecimal rate = new BigDecimal(product.getPrice());
        //         BigDecimal amount = rate.multiply(BigDecimal.valueOf(itemDto.getQuantity()));

        //         BigDecimal tax = calculateTax(amount, itemDto.getTaxType());

        //         InvoiceItem item = new InvoiceItem();
        //         item.setProduct(product);
        //         item.setInvoice(invoice);
        //         item.setDescription(itemDto.getDescription());
        //         item.setQuantity(itemDto.getQuantity());
        //         item.setRate(rate);
        //         item.setTaxAmount(tax);

        //         invoiceItems.add(item);

        //         subTotal = subTotal.add(amount);
        //         totalTax = totalTax.add(tax);
        //     }
        // }

        BigDecimal discount = BigDecimal.ZERO; // future logic
        BigDecimal total = subTotal.subtract(discount).add(totalTax);

        invoice.setSubTotal(subTotal);
        invoice.setTaxAmount(totalTax);
        invoice.setTotalAmount(total);
        invoice.setItems(invoiceItems);

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

    // Helper: Calculate tax based on taxType
    private BigDecimal calculateTax(BigDecimal lineTotal, String taxType) {
        if (taxType == null) return BigDecimal.ZERO;

        switch (taxType.toUpperCase()) {
            case "GST5":
                return lineTotal.multiply(BigDecimal.valueOf(0.05));
            case "GST18":
                return lineTotal.multiply(BigDecimal.valueOf(0.18));
            default:
                return BigDecimal.ZERO;
        }
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