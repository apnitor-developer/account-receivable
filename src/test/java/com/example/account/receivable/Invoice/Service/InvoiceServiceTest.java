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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Common.EmailService;
import com.example.account.receivable.Common.InvoiceTemplateService;
import com.example.account.receivable.Common.PdfGeneratorService;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Entity.CompanyCustomers;
import com.example.account.receivable.Customer.Entity.CustomerDunningCreditSettings;
import com.example.account.receivable.Customer.Repository.CustomerRepository;
import com.example.account.receivable.GL.Service.GlTransactionService;
import com.example.account.receivable.Invoice.Dto.InvoiceDto;
import com.example.account.receivable.Invoice.Dto.InvoiceItemDto;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Enum.InvoiceStatus;
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

    @Mock
    private GlTransactionService glTransactionService;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    void createInvoice_generatedNumberCalculatesTotals() {
        Customer customer = customerWithCompany(3L);

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
        assertEquals(InvoiceStatus.CREATED, result.getStatus());
    }

    @Test
    void createInvoice_whenManualNumberExists_throwsConflict() {
        Customer customer = customerWithCompany(1L);
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
    void createInvoice_whenCreditLimitConfigured_createsInvoice() {
        Customer customer = customerWithCompany(9L);
        CustomerDunningCreditSettings dunning = new CustomerDunningCreditSettings();
        dunning.setCreditLimit(10.0);
        customer.setDunning(dunning);

        when(customerRepository.findById(9L)).thenReturn(Optional.of(customer));
        when(invoiceRepository.findTopByInvoiceNumberStartingWithOrderByInvoiceNumberDesc("INV-"))
                .thenReturn(Optional.empty());
        when(invoiceRepository.existsByInvoiceNumber(anyString())).thenReturn(false);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice inv = invocation.getArgument(0);
            inv.setId(88L);
            return inv;
        });
        when(invoiceItemRepo.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceDto dto = new InvoiceDto();
        dto.setGenerated(true);
        dto.setInvoiceDate(LocalDate.of(2024, 5, 1));
        dto.setDueDate(LocalDate.of(2024, 5, 10));
        dto.setItems(List.of(item("Consulting", "5", 1, null)));

        Invoice invoice = invoiceService.createInvoice(9L, dto);

        assertEquals(88L, invoice.getId());
        assertEquals(new BigDecimal("5"), invoice.getSubTotal());
        assertEquals(new BigDecimal("5"), invoice.getBalanceDue());
        assertEquals(InvoiceStatus.CREATED, invoice.getStatus());
        verify(invoiceRepository).save(any(Invoice.class));
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
                .status(InvoiceStatus.OPEN)
                .build();

        Company company = new Company();
        company.setId(20L);
        company.setLegalName("Wayne Corp");

        when(invoiceRepository.findById(11L)).thenReturn(Optional.of(invoice));
        when(companyRepository.findById(20L)).thenReturn(Optional.of(company));
        when(invoiceTemplateService.generateEmailHtml(invoice, company)).thenReturn("<html>invoice</html>");
        when(invoiceTemplateService.generatePdfHtml(invoice, company)).thenReturn("<html>pdf</html>");
        byte[] pdf = "pdf".getBytes(StandardCharsets.UTF_8);
        when(pdfGeneratorService.generatePdf("<html>pdf</html>")).thenReturn(pdf);

        invoiceService.sendInvoiceEmail(11L, 20L);

        verify(emailService).sendWithAttachment(
                eq("client@example.com"),
                eq("Invoice INV-1001"),
                eq("<html>invoice</html>"),
                eq(pdf)
        );
    }

    @Test
    void getOpenAndPartialInvoices_whenMonthsProvidedUsesRelativeWindow() {
        Page<Invoice> page = new PageImpl<>(List.of());
        when(invoiceRepository.findCompanyInvoicesByStatusAndDateRange(
                eq(5L),
                any(Pageable.class),
                anyList(),
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenReturn(page);

        Page<Invoice> result = invoiceService.getOpenAndPartialInvoicesByCompanyId(
                5L,
                0,
                10,
                null,
                null,
                3
        );

        assertEquals(page, result);

        ArgumentCaptor<LocalDate> fromCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toCaptor = ArgumentCaptor.forClass(LocalDate.class);

        verify(invoiceRepository).findCompanyInvoicesByStatusAndDateRange(
                eq(5L),
                any(Pageable.class),
                eq(List.of(InvoiceStatus.CREATED, InvoiceStatus.OPEN, InvoiceStatus.PARTIAL)),
                fromCaptor.capture(),
                toCaptor.capture()
        );

        assertEquals(toCaptor.getValue().minusMonths(3), fromCaptor.getValue());
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

    private Customer customerWithCompany(Long customerId) {
        Customer customer = new Customer();
        customer.setId(customerId);

        Company company = new Company();
        company.setId(42L);
        company.setLegalName("Acme Corp");

        CompanyCustomers link = new CompanyCustomers();
        link.setCompany(company);
        link.setCustomer(customer);
        link.setUserId(1L);

        customer.getCompanyCompanies().add(link);
        return customer;
    }
}
