package com.example.account.receivable.BankReconciliation.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.BankReconciliation.Entity.BankTransaction;
import com.example.account.receivable.BankReconciliation.Enum.PaymentStatus;
import com.example.account.receivable.BankReconciliation.Repository.BankTransactionRepository;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Repository.CustomerRepository;
import com.example.account.receivable.Payment.Entity.Payment;
import com.example.account.receivable.Payment.Enum.PaymentMethod;
import com.example.account.receivable.Payment.Enum.PaymentSource;
import com.example.account.receivable.Payment.Repository.PaymentRepository;
import com.example.account.receivable.Payment.Service.PaymentService;

@ExtendWith(MockitoExtension.class)
class BankReconciliationServiceTest {

    @Mock
    private BankTransactionRepository bankTransactionRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private BankReconciliationService bankReconciliationService;

    @Test
    void processBaiFile_createsTransactionAndAppendsContinuation() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "transactions.bai",
                "text/plain",
                """
                        16,142,150.50,,REF123,Customer,Desc/
                        88,/More Details/
                        """.getBytes(StandardCharsets.UTF_8)
        );

        Company company = Company.builder().id(2L).build();
        when(companyRepository.findById(2L)).thenReturn(Optional.of(company));
        when(bankTransactionRepository.save(any(BankTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bankReconciliationService.processBaiFile(file, 2L);

        ArgumentCaptor<BankTransaction> transactionCaptor = ArgumentCaptor.forClass(BankTransaction.class);
        verify(bankTransactionRepository, times(2)).save(transactionCaptor.capture());

        List<BankTransaction> savedTransactions = transactionCaptor.getAllValues();
        BankTransaction initial = savedTransactions.get(0);
        BankTransaction updated = savedTransactions.get(1);

        assertEquals(company, initial.getCompany());
        assertEquals(new BigDecimal("150.50"), initial.getAmount());
        assertEquals("ACH Credit Receipt", initial.getTransactionType());
        assertEquals("CREDIT", initial.getDebitCredit());
        assertEquals(PaymentStatus.DRAFT, initial.getStatus());
        assertEquals(PaymentSource.BANK, initial.getSource());
        assertEquals(initial, updated);
        assertEquals("Desc More Details", updated.getDescription());
        assertEquals(LocalDate.now(), initial.getTransactionDate());
        assertEquals("Payment created via BAI file", initial.getSystemNote());
    }

    @Test
    void processBaiFile_whenCompanyMissingThrowsBadRequest() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "transactions.bai",
                "text/plain",
                "16,142,150.50,,REF123,Customer,Desc/".getBytes(StandardCharsets.UTF_8)
        );
        when(companyRepository.findById(5L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bankReconciliationService.processBaiFile(file, 5L));

        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void getBankTransactions_whenFromDateAfterToDateThrowsBadRequest() {
        LocalDate from = LocalDate.now();
        LocalDate to = from.minusDays(1);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bankReconciliationService.getBankTransactions(1L, from, to, null));

        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void getBankTransactions_withMonthsFallbackUsesRelativeWindow() {
        when(bankTransactionRepository.findByCompanyIdFiltered(
                eq(3L),
                eq(PaymentStatus.DRAFT),
                any(LocalDate.class),
                any(LocalDate.class)))
                .thenReturn(List.of());

        bankReconciliationService.getBankTransactions(3L, null, null, 3);

        ArgumentCaptor<LocalDate> fromCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toCaptor = ArgumentCaptor.forClass(LocalDate.class);

        verify(bankTransactionRepository).findByCompanyIdFiltered(
                eq(3L),
                eq(PaymentStatus.DRAFT),
                fromCaptor.capture(),
                toCaptor.capture());

        assertEquals(toCaptor.getValue().minusMonths(3), fromCaptor.getValue());
        assertEquals(LocalDate.now(), toCaptor.getValue());
    }

    @Test
    void approveAndApplyBankTransaction_createsPaymentAndUpdatesStatuses() {
        BankTransaction bankTransaction = BankTransaction.builder()
                .id(20L)
                .amount(new BigDecimal("250.00"))
                .transactionDate(LocalDate.of(2026, 2, 1))
                .status(PaymentStatus.DRAFT)
                .description("Wire payment")
                .build();

        Customer customer = new Customer();
        customer.setId(8L);
        customer.setCustomerName("Acme");

        when(bankTransactionRepository.findById(20L)).thenReturn(Optional.of(bankTransaction));
        when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            if (payment.getId() == null) {
                payment.setId(40L);
            }
            return payment;
        });

        List<Long> invoiceIds = List.of(1L, 2L);

        Payment result = bankReconciliationService.approveAndApplyBankTransaction(20L, 5L, invoiceIds);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentService).applyInvoices(paymentCaptor.capture(), eq(invoiceIds));
        Payment appliedPayment = paymentCaptor.getValue();

        assertEquals(new BigDecimal("250.00"), appliedPayment.getPaymentAmount());
        assertEquals(PaymentMethod.BANK_TRANSFER, appliedPayment.getPaymentMethod());
        assertEquals(PaymentSource.BANK, appliedPayment.getSource());
        assertEquals(bankTransaction, appliedPayment.getBankTransaction());
        assertEquals(customer, appliedPayment.getCustomer());

        assertEquals(PaymentStatus.APPROVED, result.getStatus());
        assertEquals(PaymentStatus.APPLIED, bankTransaction.getStatus());

        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(bankTransactionRepository).save(bankTransaction);
    }

    @Test
    void approveAndApplyBankTransaction_whenAlreadyProcessedThrows() {
        BankTransaction bankTransaction = BankTransaction.builder()
                .id(11L)
                .status(PaymentStatus.APPROVED)
                .build();

        when(bankTransactionRepository.findById(11L)).thenReturn(Optional.of(bankTransaction));

        assertThrows(IllegalStateException.class,
                () -> bankReconciliationService.approveAndApplyBankTransaction(11L, 3L, List.of()));

        verify(customerRepository, never()).findById(any());
        verify(paymentRepository, never()).save(any());
    }
}
