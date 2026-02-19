package com.example.account.receivable.Payment.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
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

import com.example.account.receivable.BankReconciliation.Enum.PaymentStatus;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Repository.CustomerRepository;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Enum.InvoiceStatus;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;
import com.example.account.receivable.Payment.Dto.ReceivePaymentRequest;
import com.example.account.receivable.Payment.Dto.ResponseDTO.ManualPaymentResponseDto;
import com.example.account.receivable.Payment.Dto.ResponseDTO.MonthlyPaymentDto;
import com.example.account.receivable.Payment.Dto.ResponseDTO.PaymentReportDto;
import com.example.account.receivable.Payment.Entity.Payment;
import com.example.account.receivable.Payment.Entity.PaymentApplication;
import com.example.account.receivable.Payment.Enum.PaymentMethod;
import com.example.account.receivable.Payment.Enum.PaymentSource;
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

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createManualPayment_whenCustomerMissing_throwsNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        ReceivePaymentRequest request = new ReceivePaymentRequest();
        request.setPaymentAmount(BigDecimal.TEN);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> paymentService.createManualPayment(1L, request));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createManualPayment_savesPaymentAndMapsResponse() {
        Customer customer = new Customer();
        customer.setId(2L);
        customer.setCustomerName("Globex");

        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer));

        ReceivePaymentRequest request = new ReceivePaymentRequest();
        request.setBankDeposit(new BigDecimal("150"));
        request.setServiceFee(new BigDecimal("5"));
        request.setPaymentAmount(new BigDecimal("155"));
        request.setPaymentMethod(PaymentMethod.CASH);
        request.setNotes("Manual entry");

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(20L);
            payment.setCreatedAt(Instant.parse("2026-02-17T10:15:30Z"));
            payment.setCustomer(customer);
            return payment;
        });

        ManualPaymentResponseDto response = paymentService.createManualPayment(2L, request);

        assertEquals(20L, response.getPaymentId());
        assertEquals(new BigDecimal("150"), response.getBankDeposit());
        assertEquals("Globex", response.getCustomerName());

        verify(paymentRepository).save(argThat(payment ->
                payment.getCustomer().equals(customer)
                        && payment.getPaymentMethod() == PaymentMethod.CASH
                        && payment.getSource() == PaymentSource.MANUAL
                        && payment.getStatus() == PaymentStatus.CREATED
        ));
    }

    @Test
    void approveAndApplyPayment_whenPaymentMissing_throwsNotFound() {
        when(paymentRepository.findById(5L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> paymentService.approveAndApplyPayment(5L, List.of(1L)));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void approveAndApplyPayment_whenStatusNotCreated_throwsIllegalState() {
        Payment payment = Payment.builder()
                .id(6L)
                .status(PaymentStatus.APPROVED)
                .build();

        when(paymentRepository.findById(6L)).thenReturn(Optional.of(payment));

        assertThrows(IllegalStateException.class,
                () -> paymentService.approveAndApplyPayment(6L, List.of()));
        verify(invoiceRepository, never()).findAllById(any());
    }

    @Test
    void approveAndApplyPayment_appliesInvoicesAndMarksApproved() {
        Payment payment = Payment.builder()
                .id(7L)
                .paymentAmount(new BigDecimal("180"))
                .status(PaymentStatus.CREATED)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        Invoice first = Invoice.builder()
                .id(1L)
                .balanceDue(new BigDecimal("100"))
                .status(InvoiceStatus.OPEN)
                .build();
        Invoice second = Invoice.builder()
                .id(2L)
                .balanceDue(new BigDecimal("80"))
                .status(InvoiceStatus.OPEN)
                .build();

        when(paymentRepository.findById(7L)).thenReturn(Optional.of(payment));
        when(invoiceRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(first, second));
        when(paymentApplicationRepository.save(any(PaymentApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = paymentService.approveAndApplyPayment(7L, List.of(1L, 2L));

        assertEquals(PaymentStatus.APPROVED, result.getStatus());
        assertEquals(BigDecimal.ZERO, first.getBalanceDue());
        assertEquals(BigDecimal.ZERO, second.getBalanceDue());
        assertEquals(InvoiceStatus.PAID, first.getStatus());
        assertEquals(InvoiceStatus.PAID, second.getStatus());

        verify(paymentApplicationRepository, times(2)).save(any(PaymentApplication.class));
    }

    @Test
    void getCreatedPayments_withMonthsFilter_callsRepository() {
        Customer customer = new Customer();
        customer.setId(9L);
        customer.setCustomerName("Acme");

        Payment payment = Payment.builder()
                .id(30L)
                .customer(customer)
                .bankDeposit(BigDecimal.TEN)
                .paymentAmount(BigDecimal.TEN)
                .paymentMethod(PaymentMethod.CASH)
                .paymentDate(LocalDate.now())
                .source(PaymentSource.MANUAL)
                .status(PaymentStatus.CREATED)
                .build();

        Page<Payment> page = new PageImpl<>(List.of(payment));

        when(paymentRepository.findPaymentsByCompanyStatusAndDateRange(
                eq(8L),
                eq(PaymentStatus.CREATED),
                any(),
                any(),
                any(Pageable.class)
        )).thenReturn(page);

        Page<ManualPaymentResponseDto> result =
                paymentService.getCreatedPayments(8L, 0, 5, null, null, 2);

        assertEquals(1, result.getContent().size());
        assertEquals(30L, result.getContent().get(0).getPaymentId());

        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCaptor = ArgumentCaptor.forClass(Instant.class);

        verify(paymentRepository).findPaymentsByCompanyStatusAndDateRange(
                eq(8L),
                eq(PaymentStatus.CREATED),
                fromCaptor.capture(),
                toCaptor.capture(),
                any(Pageable.class)
        );
        assertNotNull(fromCaptor.getValue());
        assertNotNull(toCaptor.getValue());
        assertTrue(fromCaptor.getValue().isBefore(toCaptor.getValue()));
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

        when(paymentRepository.findPaymentsByCompanyIdFilteredAndStatus(
                eq(9L),
                eq(PaymentStatus.APPROVED),
                eq(from),
                eq(to),
                any(Pageable.class)
        )).thenReturn(expected);

        Page<Payment> result = paymentService.getPaymentsByCompanyId(9L, 0, 5, from, to, null);

        assertEquals(expected, result);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(paymentRepository).findPaymentsByCompanyIdFilteredAndStatus(
                eq(9L),
                eq(PaymentStatus.APPROVED),
                eq(from),
                eq(to),
                pageableCaptor.capture());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(5, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void getPaymentsByCompanyId_whenMonthsProvidedUsesRelativeWindow() {
        Page<Payment> expected = new PageImpl<>(List.of());
        when(paymentRepository.findPaymentsByCompanyIdFilteredAndStatus(
                eq(3L),
                eq(PaymentStatus.APPROVED),
                any(LocalDate.class),
                any(LocalDate.class),
                any(Pageable.class)
        )).thenReturn(expected);

        Page<Payment> result = paymentService.getPaymentsByCompanyId(3L, 1, 25, null, null, 4);

        assertEquals(expected, result);

        ArgumentCaptor<LocalDate> fromCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toCaptor = ArgumentCaptor.forClass(LocalDate.class);

        verify(paymentRepository).findPaymentsByCompanyIdFilteredAndStatus(
                eq(3L),
                eq(PaymentStatus.APPROVED),
                fromCaptor.capture(),
                toCaptor.capture(),
                any(Pageable.class));
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
