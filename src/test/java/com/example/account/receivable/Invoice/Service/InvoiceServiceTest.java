package com.example.account.receivable.Invoice.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Common.EmailService;
import com.example.account.receivable.Common.InvoiceTemplateService;
import com.example.account.receivable.Common.PdfGeneratorService;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Entity.CustomerDunningCreditSettings;
import com.example.account.receivable.Customer.Repository.CustomerRepository;
import com.example.account.receivable.Invoice.Dto.InvoiceDto;
import com.example.account.receivable.Invoice.Dto.InvoiceItemDto;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Repository.InvoiceItemRepo;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private InvoiceTemplateService invoiceTemplateService;

    @Mock
    private PdfGeneratorService pdfGeneratorService;

    @Mock
    private InvoiceItemRepo invoiceItemRepo;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    void createInvoice_generatedNumberCalculatesTotals() {
        Customer customer = new Customer();
        customer.setId(3L);

        when(customerRepository.findById(3L)).thenReturn(Optional.of(customer));
        when(invoiceRepository.findTopByInvoiceNumberStartingWithOrderByInvoiceNumberDesc("INV-"))
                .thenReturn(Optional.of(Invoice.builder().invoiceNumber("INV-0007").build()));
        when(invoiceRepository.existsByInvoiceNumber(anyString())).thenReturn(false);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice inv = invocation.getArgument(0);
            inv.setId(20L);
            return inv;
        });
        when(invoiceItemRepo.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceDto dto = new InvoiceDto();
        dto.setGenerated(true);
        dto.setInvoiceDate(LocalDate.of(2024, 1, 1));
        dto.setDueDate(LocalDate.of(2024, 1, 15));
        dto.setItems(List.of(
                item("Implementation", "50", 2, "10%"),
                item("Support", "20", 1, null)
        ));

        Invoice result = invoiceService.createInvoice(3L, dto);

        assertEquals("INV-0008", result.getInvoiceNumber());
        assertEquals(new BigDecimal("120"), result.getSubTotal());
        assertEquals(new BigDecimal("130"), result.getTotalAmount());
        assertEquals(new BigDecimal("130"), result.getBalanceDue());
        assertEquals("OPEN", result.getStatus());
    }

    @Test
    void createInvoice_whenManualNumberExists_throwsConflict() {
        Customer customer = new Customer();
        customer.setId(1L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(invoiceRepository.existsByInvoiceNumber("INV-9000")).thenReturn(true);

        InvoiceDto dto = new InvoiceDto();
        dto.setGenerated(false);
        dto.setInvoiceNumber("INV-9000");
        dto.setItems(List.of(item("Line1", "10", 1, null)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> invoiceService.createInvoice(1L, dto));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void createInvoice_whenCreditLimitExceeded_throwsBadRequest() {
        Customer customer = new Customer();
        customer.setId(9L);
        CustomerDunningCreditSettings dunning = new CustomerDunningCreditSettings();
        dunning.setCreditLimit(10.0);
        customer.setDunning(dunning);

        when(customerRepository.findById(9L)).thenReturn(Optional.of(customer));
        when(invoiceRepository.findTopByInvoiceNumberStartingWithOrderByInvoiceNumberDesc("INV-"))
                .thenReturn(Optional.empty());
        when(invoiceRepository.existsByInvoiceNumber(anyString())).thenReturn(false);
        when(invoiceRepository.getCustomerOutstandingBalance(9L)).thenReturn(new BigDecimal("8"));

        InvoiceDto dto = new InvoiceDto();
        dto.setGenerated(true);
        dto.setItems(List.of(item("Consulting", "5", 1, null)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> invoiceService.createInvoice(9L, dto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void sendInvoiceEmail_buildsTemplateAndSends() {
        Customer customer = new Customer();
        customer.setId(7L);
        customer.setEmail("client@example.com");

        Invoice invoice = Invoice.builder()
                .id(11L)
                .invoiceNumber("INV-1001")
                .customer(customer)
                .build();

        when(invoiceRepository.findById(11L)).thenReturn(Optional.of(invoice));
        when(invoiceTemplateService.generateHtml(invoice)).thenReturn("<html>invoice</html>");
        byte[] pdf = "pdf".getBytes(StandardCharsets.UTF_8);
        when(pdfGeneratorService.generatePdf("<html>invoice</html>")).thenReturn(pdf);

        invoiceService.sendInvoiceEmail(11L);

        verify(emailService).sendWithAttachment(
                eq("client@example.com"),
                eq("Invoice INV-1001"),
                eq("<html>invoice</html>"),
                eq(pdf)
        );
    }

    private InvoiceItemDto item(String name, String rate, int quantity, String tax) {
        InvoiceItemDto dto = new InvoiceItemDto();
        dto.setItemName(name);
        dto.setRate(new BigDecimal(rate));
        dto.setQuantity(quantity);
        dto.setTax(tax);
        dto.setDescription("desc");
        return dto;
    }
}
