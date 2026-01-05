package com.example.account.receivable.Collections.Reminder.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceTemplateService invoiceTemplateService;

    @Mock
    private PdfGeneratorService pdfGeneratorService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReminderService reminderService;

    @Test
    void sendInvoiceReminder_whenInvoiceValid_sendsEmailWithArtifacts() {
        Invoice invoice = buildInvoice(false, new BigDecimal("120.00"), LocalDate.now().minusDays(5));
        when(invoiceRepository.findById(55L)).thenReturn(Optional.of(invoice));

        String html = "<html>reminder</html>";
        byte[] pdf = new byte[] {1, 2, 3};
        when(invoiceTemplateService.generateHtmlReminder(invoice)).thenReturn(html);
        when(pdfGeneratorService.generatePdf(html)).thenReturn(pdf);

        reminderService.sendInvoiceReminder(55L);

        verify(invoiceTemplateService).generateHtmlReminder(invoice);
        verify(pdfGeneratorService).generatePdf(html);
        verify(emailService).sendWithAttachment(eq("customer@example.com"), anyString(), eq(html), eq(pdf));
    }

    @Test
    void sendInvoiceReminder_whenInvoiceMissing_throwsNotFound() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> reminderService.sendInvoiceReminder(1L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verifyNoInteractions(invoiceTemplateService, pdfGeneratorService, emailService);
    }

    @Test
    void sendInvoiceReminder_whenInvoiceDeleted_throwsBadRequest() {
        Invoice invoice = buildInvoice(true, new BigDecimal("50"), LocalDate.now().minusDays(1));
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> reminderService.sendInvoiceReminder(1L));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(invoiceTemplateService, pdfGeneratorService, emailService);
    }

    @Test
    void sendInvoiceReminder_whenInvoiceHasNoBalance_throwsBadRequest() {
        Invoice invoice = buildInvoice(false, BigDecimal.ZERO, LocalDate.now().minusDays(1));
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> reminderService.sendInvoiceReminder(1L));
        assertEquals("Invoice has no pending balance", ex.getReason());
        verifyNoInteractions(invoiceTemplateService, pdfGeneratorService, emailService);
    }

    @Test
    void sendInvoiceReminder_whenInvoiceNotOverdue_throwsBadRequest() {
        Invoice invoice = buildInvoice(false, new BigDecimal("10"), LocalDate.now().plusDays(2));
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> reminderService.sendInvoiceReminder(1L));
        assertEquals("Invoice is not overdue", ex.getReason());
        verifyNoInteractions(invoiceTemplateService, pdfGeneratorService, emailService);
    }

    private Invoice buildInvoice(boolean deleted, BigDecimal balance, LocalDate dueDate) {
        Customer customer = new Customer();
        customer.setEmail("customer@example.com");

        Invoice invoice = Invoice.builder()
            .id(99L)
            .invoiceNumber("INV-1000")
            .balanceDue(balance)
            .dueDate(dueDate)
            .customer(customer)
            .build();
        invoice.setDeleted(deleted);
        return invoice;
    }
}
