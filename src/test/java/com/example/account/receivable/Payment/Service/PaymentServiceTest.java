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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Collections.PromiseToPay.Entity.PromiseStatus;
import com.example.account.receivable.Collections.PromiseToPay.Entity.PromiseToPay;
import com.example.account.receivable.Collections.PromiseToPay.Repository.PromiseToPayRepo;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Repository.CustomerRepository;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;
import com.example.account.receivable.Payment.Dto.ReceivePaymentRequest;
import com.example.account.receivable.Payment.Entity.Payment;
import com.example.account.receivable.Payment.Entity.PaymentApplication;
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
        Customer customer = new Customer();
        customer.setId(1L);

        Invoice first = Invoice.builder()
                .id(10L)
                .balanceDue(new BigDecimal("100"))
                .status("OPEN")
                .build();
        Invoice second = Invoice.builder()
                .id(11L)
                .balanceDue(new BigDecimal("80"))
                .status("OPEN")
                .build();

        ReceivePaymentRequest request = new ReceivePaymentRequest();
        request.setPaymentAmount(new BigDecimal("150"));
        request.setPaymentMethod("ACH");
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
        assertEquals("PAID", first.getStatus());
        assertEquals("PARTIAL", second.getStatus());
        assertEquals(PromiseStatus.COMPLETED, promise1.getStatus());
        assertEquals(PromiseStatus.DUE_TODAY, promise2.getStatus());

        verify(paymentRepository).save(any(Payment.class));
        verify(paymentApplicationRepository, times(2)).save(any(PaymentApplication.class));
        verify(invoiceRepository, times(2)).save(any(Invoice.class));
        verify(promiseToPayRepo).save(eq(promise1));
    }
}
