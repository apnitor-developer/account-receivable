package com.example.account.receivable.Invoice.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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

@Service
public class InvoiceService {
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final ProductAndServiceRepository productRepository;

    public InvoiceService(
        CustomerRepository customerRepository,
        InvoiceRepository invoiceRepository,
        ProductAndServiceRepository productRepository
    ){
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
    }

    // public Invoice createInvoice(Long customerId , InvoiceDto invoicedto) {
    //     Customer customer = customerRepository.findById(customerId)
    //                         .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND , "Customer not found"));

    //     Invoice invoice = Invoice.builder()
    //             .customer(customer)
    //             .invoiceNumber(invoicedto.getInvoiceNumber())
    //             .invoiceDate(invoicedto.getInvoiceDate())
    //             .dueDate(invoicedto.getDueDate())
    //             .subTotal(invoicedto.getSubTotal())
    //             .taxAmount(invoicedto.getTaxAmount())
    //             .totalAmount(invoicedto.getTotalAmount())
    //             .note(invoicedto.getNote())
    //             .build();

        


    //     return invoiceRepository.save(invoice);
    // }


    // Create invoice and the invoice_items
    public Invoice createInvoice(Long customerId , InvoiceDto dto) {

        // Validate Customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        // Create Invoice
        Invoice invoice = Invoice.builder()
                .invoiceNumber(dto.getInvoiceNumber())
                .invoiceDate(dto.getInvoiceDate())
                .dueDate(dto.getDueDate())
                .note(dto.getNote())
                .customer(customer)
                .deleted(false)
                .active(true)
                .build();

        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        List<InvoiceItem> invoiceItems = new ArrayList<>();

        // Loop through Items
        for (InvoiceItemDto itemDto : dto.getItems()) {

                ProductAndService product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

                BigDecimal rate = new BigDecimal(product.getPrice());
                BigDecimal amount = rate.multiply(BigDecimal.valueOf(itemDto.getQuantity()));

                BigDecimal tax = calculateTax(amount, itemDto.getTaxType());

                InvoiceItem item = new InvoiceItem();
                item.setProduct(product);
                item.setInvoice(invoice);
                item.setDescription(itemDto.getDescription());
                item.setQuantity(itemDto.getQuantity());
                item.setRate(rate);
                item.setTaxAmount(tax);

                invoiceItems.add(item);

                subTotal = subTotal.add(amount);
                totalTax = totalTax.add(tax);
            }

            BigDecimal discount = BigDecimal.ZERO; // future logic
            BigDecimal total = subTotal.subtract(discount).add(totalTax);

            invoice.setSubTotal(subTotal);
            invoice.setTaxAmount(totalTax);
            invoice.setTotalAmount(total);
            invoice.setItems(invoiceItems);

            return invoiceRepository.save(invoice);
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

    //Get all Invoices
    public Page<Invoice> getAllInvoices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return invoiceRepository.findByDeletedFalse(pageable);
    }

    //Get Single customer invoice
    public List<Invoice> getSingleCustomerInvoice(Long customerId) {
        return invoiceRepository.findByCustomerIdAndDeletedFalse(customerId);
    }


    //Invoice By Id
    public Invoice getInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND , "Invoice not found with this Id"));

        return invoice;
    }
}
