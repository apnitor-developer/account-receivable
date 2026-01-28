package com.example.account.receivable.Payment.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Collections.PromiseToPay.Entity.PromiseStatus;
import com.example.account.receivable.Collections.PromiseToPay.Entity.PromiseToPay;
import com.example.account.receivable.Collections.PromiseToPay.Repository.PromiseToPayRepo;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Customer.Entity.CompanyCustomers;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Repository.CustomerRepository;
import com.example.account.receivable.GL.Service.GlTransactionService;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Enum.InvoiceStatus;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;
import com.example.account.receivable.Payment.Dto.ReceivePaymentRequest;
import com.example.account.receivable.Payment.Dto.ResponseDTO.MonthlyPaymentDto;
import com.example.account.receivable.Payment.Dto.ResponseDTO.PaymentReportDto;
import com.example.account.receivable.Payment.Entity.Payment;
import com.example.account.receivable.Payment.Entity.PaymentApplication;
import com.example.account.receivable.Payment.Enum.PaymentMethod;
import com.example.account.receivable.Payment.MonthlyPaymentProjection;
import com.example.account.receivable.Payment.PaymentMethodReportProjection;
import com.example.account.receivable.Payment.Repository.PaymentApplicationRepository;
import com.example.account.receivable.Payment.Repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentApplicationRepository paymentApplicationRepository;

    @Mock
    private PromiseToPayRepo promiseToPayRepo;

    @Mock
    private GlTransactionService glTransactionService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void applyPayment_whenCustomerMissing_throwsNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        ReceivePaymentRequest request = new ReceivePaymentRequest();
        request.setPaymentAmount(BigDecimal.TEN);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> paymentService.applyPayment(1L, request));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void applyPayment_appliesAmountsAndUpdatesPromises() {
        Customer customer = customerWithCompany(1L);

        Invoice first = Invoice.builder()
                .id(10L)
                .balanceDue(new BigDecimal("100"))
                .status(InvoiceStatus.OPEN)
                .build();
        Invoice second = Invoice.builder()
                .id(11L)
                .balanceDue(new BigDecimal("80"))
                .status(InvoiceStatus.OPEN)
                .build();

        ReceivePaymentRequest request = new ReceivePaymentRequest();
        request.setPaymentAmount(new BigDecimal("150"));
        request.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        request.setInvoiceIds(List.of(10L, 11L));

        PromiseToPay promise1 = PromiseToPay.builder()
                .id(1L)
                .customer(customer)
                .status(PromiseStatus.PENDING)
                .amountPromised(new BigDecimal("100"))
                .promiseDate(LocalDate.now().minusDays(1))
                .build();
        PromiseToPay promise2 = PromiseToPay.builder()
                .id(2L)
                .customer(customer)
                .status(PromiseStatus.DUE_TODAY)
                .amountPromised(new BigDecimal("75"))
                .promiseDate(LocalDate.now())
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(invoiceRepository.findAllById(request.getInvoiceIds())).thenReturn(List.of(first, second));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(5L);
            return payment;
        });
        when(promiseToPayRepo.findByCustomerId(1L)).thenReturn(List.of(promise1, promise2));
        when(paymentApplicationRepository.save(any(PaymentApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(promiseToPayRepo.save(any(PromiseToPay.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = paymentService.applyPayment(1L, request);

        assertEquals(5L, result.getId());
        assertEquals(BigDecimal.ZERO, first.getBalanceDue());
        assertEquals(new BigDecimal("30"), second.getBalanceDue());
        assertEquals(InvoiceStatus.PAID, first.getStatus());
        assertEquals(InvoiceStatus.PARTIAL, second.getStatus());
        assertEquals(PromiseStatus.COMPLETED, promise1.getStatus());
        assertEquals(PromiseStatus.DUE_TODAY, promise2.getStatus());

        verify(paymentRepository).save(any(Payment.class));
        verify(paymentApplicationRepository, times(2)).save(any(PaymentApplication.class));
        verify(invoiceRepository, times(2)).save(any(Invoice.class));
        verify(promiseToPayRepo).save(eq(promise1));
    }

    @Test
    void getPaymentsByCompanyId_withInvalidDateRange_throwsBadRequest() {
        LocalDate from = LocalDate.now();
        LocalDate to = from.minusDays(1);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> paymentService.getPaymentsByCompanyId(5L, 0, 10, from, to, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void getPaymentsByCompanyId_withFilters_returnsPagedResult() {
        LocalDate from = LocalDate.now().minusDays(5);
        LocalDate to = LocalDate.now();
        Page<Payment> expected = new PageImpl<>(List.of(Payment.builder().id(1L).build()));

        when(paymentRepository.findPaymentsByCompanyIdFiltered(eq(9L), eq(from), eq(to), any(Pageable.class)))
                .thenReturn(expected);

        Page<Payment> result = paymentService.getPaymentsByCompanyId(9L, 0, 5, from, to, null);

        assertEquals(expected, result);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(paymentRepository).findPaymentsByCompanyIdFiltered(eq(9L), eq(from), eq(to), pageableCaptor.capture());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(5, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void getPaymentsByCompanyId_whenMonthsProvidedUsesRelativeWindow() {
        Page<Payment> expected = new PageImpl<>(List.of());
        when(paymentRepository.findPaymentsByCompanyIdFiltered(eq(3L), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(expected);

        Page<Payment> result = paymentService.getPaymentsByCompanyId(3L, 1, 25, null, null, 4);

        assertEquals(expected, result);

        ArgumentCaptor<LocalDate> fromCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toCaptor = ArgumentCaptor.forClass(LocalDate.class);

        verify(paymentRepository).findPaymentsByCompanyIdFiltered(eq(3L), fromCaptor.capture(), toCaptor.capture(), any(Pageable.class));
        assertEquals(toCaptor.getValue().minusMonths(4), fromCaptor.getValue());
    }

    @Test
    void getPaymentReport_aggregatesCounts() {
        PaymentMethodReportProjection cash = new PaymentReportProjectionStub(PaymentMethod.CASH, 3L);
        PaymentMethodReportProjection wire = new PaymentReportProjectionStub(PaymentMethod.BANK_TRANSFER, 2L);

        when(paymentRepository.getPaymentReport(eq(4L), any(LocalDate.class))).thenReturn(List.of(cash, wire));

        PaymentReportDto dto = paymentService.getPaymentReport(4L, 6);

        assertEquals(2, dto.getMethodCounts().size());
        assertEquals(5L, dto.getTotalPayments());
        assertEquals(3L, dto.getMethodCounts().get(PaymentMethod.CASH));

        ArgumentCaptor<LocalDate> fromCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(paymentRepository).getPaymentReport(eq(4L), fromCaptor.capture());
        assertEquals(LocalDate.now().minusMonths(6), fromCaptor.getValue());
    }

    private Customer customerWithCompany(Long customerId) {
        Customer customer = new Customer();
        customer.setId(customerId);

        Company company = new Company();
        company.setId(99L);
        company.setLegalName("Acme");

        CompanyCustomers link = new CompanyCustomers();
        link.setCompany(company);
        link.setCustomer(customer);
        link.setUserId(1L);

        customer.getCompanyCompanies().add(link);
        return customer;
    }

    @Test
    void getMonthlyPaymentsByYear_fillsMissingMonths() {
        MonthlyPaymentProjection jan = new MonthlyPaymentProjectionStub(2026, 1, new BigDecimal("100"));
        MonthlyPaymentProjection mar = new MonthlyPaymentProjectionStub(2026, 3, new BigDecimal("50"));

        when(paymentRepository.getMonthlyPaymentsByYear(7L, 2026)).thenReturn(List.of(jan, mar));

        List<MonthlyPaymentDto> result = paymentService.getMonthlyPaymentsByYear(7L, 2026);

        assertEquals(12, result.size());
        assertEquals(new BigDecimal("100"), result.get(0).getTotalAmount());
        assertEquals(BigDecimal.ZERO, result.get(1).getTotalAmount());
        assertEquals(new BigDecimal("50"), result.get(2).getTotalAmount());
        assertEquals("Jan", result.get(0).getMonth());
        assertEquals("Feb", result.get(1).getMonth());
    }

    private static class PaymentReportProjectionStub implements PaymentMethodReportProjection {
        private final PaymentMethod method;
        private final Long count;

        PaymentReportProjectionStub(PaymentMethod method, Long count) {
            this.method = method;
            this.count = count;
        }

        @Override
        public PaymentMethod getPaymentMethod() {
            return method;
        }

        @Override
        public Long getCount() {
            return count;
        }
    }

    private static class MonthlyPaymentProjectionStub implements MonthlyPaymentProjection {
        private final Integer year;
        private final Integer month;
        private final BigDecimal total;

        MonthlyPaymentProjectionStub(Integer year, Integer month, BigDecimal total) {
            this.year = year;
            this.month = month;
            this.total = total;
        }

        @Override
        public Integer getYear() {
            return year;
        }

        @Override
        public Integer getMonth() {
            return month;
        }

        @Override
        public BigDecimal getTotal() {
            return total;
        }
    }
}
