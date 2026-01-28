package com.example.account.receivable.Aging.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.account.receivable.AgingReports.DTO.AgingReportResponse;
import com.example.account.receivable.AgingReports.DTO.CustomerAgingDto;
import com.example.account.receivable.AgingReports.Service.AgingReportService;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Enum.InvoiceStatus;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;

@ExtendWith(MockitoExtension.class)
class AgingReportServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private AgingReportService agingReportService;

    @Test
    void getAgingReport_distributesBalancesIntoBuckets() {
        LocalDate asOf = LocalDate.of(2024, 6, 30);
        Customer customer = buildCustomer(1L, "Acme");

        Invoice current = invoice(customer, InvoiceStatus.OPEN, asOf.plusDays(3), "20");
        Invoice bucket30 = invoice(customer, InvoiceStatus.OPEN, asOf.minusDays(10), "30");
        Invoice bucket60 = invoice(customer, InvoiceStatus.OPEN, asOf.minusDays(45), "40");
        Invoice bucket90 = invoice(customer, InvoiceStatus.OPEN, asOf.minusDays(70), "10");
        Invoice over90 = invoice(customer, InvoiceStatus.OPEN, asOf.minusDays(130), "50");

        when(invoiceRepository.findOpenInvoicesByCompany(5L))
                .thenReturn(List.of(current, bucket30, bucket60, bucket90, over90));

        AgingReportResponse response = agingReportService.getAgingReport(5L, asOf, null, null);
        CustomerAgingDto row = response.getRows().get(0);

        assertEquals(new BigDecimal("150"), row.getTotalDue());
        assertEquals(new BigDecimal("20"), row.getCurrent());
        assertEquals(new BigDecimal("30"), row.getBucket1To30());
        assertEquals(new BigDecimal("40"), row.getBucket31To60());
        assertEquals(new BigDecimal("10"), row.getBucket61To90());
        assertEquals(new BigDecimal("50"), row.getBucketGt90());
    }

    @Test
    void getAgingReport_appliesCustomerAndStatusFilters() {
        LocalDate asOf = LocalDate.of(2024, 6, 30);
        Customer customer1 = buildCustomer(1L, "Acme");
        Customer customer2 = buildCustomer(2L, "Globex");

        Invoice targetInvoice = invoice(customer1, InvoiceStatus.OPEN, asOf.minusDays(5), "25");
        Invoice wrongCustomer = invoice(customer2, InvoiceStatus.OPEN, asOf.minusDays(15), "30");
        Invoice wrongStatus = invoice(customer1, InvoiceStatus.PAID, asOf.minusDays(20), "40");

        when(invoiceRepository.findOpenInvoicesByCompany(9L))
                .thenReturn(List.of(targetInvoice, wrongCustomer, wrongStatus));

        AgingReportResponse response = agingReportService.getAgingReport(9L, asOf, 1L, "OPEN");

        assertEquals(1, response.getRows().size());
        CustomerAgingDto row = response.getRows().get(0);
        assertEquals(customer1.getId(), row.getCustomerId());
        assertEquals(new BigDecimal("25"), row.getTotalDue());
    }

    private Customer buildCustomer(Long id, String name) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setCustomerName(name);
        return customer;
    }

    private Invoice invoice(Customer customer, InvoiceStatus status, LocalDate dueDate, String balance) {
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setStatus(status);
        invoice.setDueDate(dueDate);
        invoice.setBalanceDue(new BigDecimal(balance));
        return invoice;
    }
}
