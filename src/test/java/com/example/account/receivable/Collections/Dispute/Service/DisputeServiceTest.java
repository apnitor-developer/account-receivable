package com.example.account.receivable.Collections.Dispute.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.account.receivable.Collections.Dispute.DTO.DisputeDTORequest;
import com.example.account.receivable.Collections.Dispute.DTO.DisputeStatusUpdateRequest;
import com.example.account.receivable.Collections.Dispute.Entity.Dispute;
import com.example.account.receivable.Collections.Dispute.Enum.DisputeStatus;
import com.example.account.receivable.Collections.Dispute.Repository.DisputeRepository;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Repository.CustomerRepository;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;

@ExtendWith(MockitoExtension.class)
class DisputeServiceTest {

    @Mock
    private DisputeRepository disputeRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private DisputeService disputeService;

    @Test
    void createDispute_whenInvoiceNotBelongToCustomer_throws() {
        DisputeDTORequest request = buildRequest();

        Customer customer = new Customer();
        customer.setId(1L);
        Customer otherCustomer = new Customer();
        otherCustomer.setId(2L);

        Invoice invoice = Invoice.builder()
                .id(10L)
                .customer(otherCustomer)
                .totalAmount(new BigDecimal("200"))
                .build();

        when(customerRepository.findById(request.getCustomerId())).thenReturn(Optional.of(customer));
        when(invoiceRepository.findById(request.getInvoiceId())).thenReturn(Optional.of(invoice));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> disputeService.createDispute(request));
        assertEquals("Invoice does not belong to this customer", ex.getMessage());
    }

    @Test
    void createDispute_whenFieldsValid_persistsDispute() {
        DisputeDTORequest request = buildRequest();

        Customer customer = new Customer();
        customer.setId(1L);

        Invoice invoice = Invoice.builder()
                .id(10L)
                .customer(customer)
                .totalAmount(new BigDecimal("500"))
                .build();

        when(customerRepository.findById(request.getCustomerId())).thenReturn(Optional.of(customer));
        when(invoiceRepository.findById(request.getInvoiceId())).thenReturn(Optional.of(invoice));
        when(disputeRepository.findTopByDisputeIdStartingWithOrderByDisputeIdDesc(anyString())).thenReturn(Optional.empty());
        when(disputeRepository.existsByDisputeId(anyString())).thenReturn(false);

        Dispute saved = Dispute.builder().disputeId("DSP-0001").build();
        when(disputeRepository.save(any(Dispute.class))).thenReturn(saved);

        Dispute result = disputeService.createDispute(request);

        assertEquals("DSP-0001", result.getDisputeId());
        verify(disputeRepository).save(any(Dispute.class));
    }

    @Test
    void createDispute_whenAmountExceedsInvoice_throws() {
        DisputeDTORequest request = buildRequest();
        request.setDisputedAmount("600");

        Customer customer = new Customer();
        customer.setId(1L);
        Invoice invoice = Invoice.builder()
                .id(10L)
                .customer(customer)
                .totalAmount(new BigDecimal("500"))
                .build();

        when(customerRepository.findById(request.getCustomerId())).thenReturn(Optional.of(customer));
        when(invoiceRepository.findById(request.getInvoiceId())).thenReturn(Optional.of(invoice));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> disputeService.createDispute(request));
        assertEquals("Disputed amount cannot exceed invoice amount", ex.getMessage());
    }

    @Test
    void updateDisputeStatus_allowsValidTransitions() {
        Dispute dispute = Dispute.builder()
                .id(1L)
                .status(DisputeStatus.OPEN)
                .build();
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(dispute));
        when(disputeRepository.save(dispute)).thenReturn(dispute);

        DisputeStatusUpdateRequest update = new DisputeStatusUpdateRequest("UNDER_REVIEW");

        Dispute updated = disputeService.updateDisputeStatus(1L, update);

        assertEquals(DisputeStatus.UNDER_REVIEW, updated.getStatus());
        verify(disputeRepository).save(dispute);
    }

    @Test
    void updateDisputeStatus_disallowsClosedTransitions() {
        Dispute dispute = Dispute.builder()
                .id(5L)
                .status(DisputeStatus.CLOSED)
                .build();
        when(disputeRepository.findById(5L)).thenReturn(Optional.of(dispute));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> disputeService.updateDisputeStatus(5L, new DisputeStatusUpdateRequest("OPEN")));
        assertEquals("Closed dispute status cannot be changed", ex.getMessage());
    }

    @Test
    void updateDisputeStatus_invalidStatusString_throws() {
        Dispute dispute = Dispute.builder()
                .id(3L)
                .status(DisputeStatus.OPEN)
                .build();
        when(disputeRepository.findById(3L)).thenReturn(Optional.of(dispute));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> disputeService.updateDisputeStatus(3L, new DisputeStatusUpdateRequest("unknown")));
        assertEquals("Invalid dispute status: unknown", ex.getMessage());
    }

    private DisputeDTORequest buildRequest() {
        DisputeDTORequest request = new DisputeDTORequest();
        request.setCustomerId(1L);
        request.setInvoiceId(10L);
        request.setDisputeCode("PRICE");
        request.setDisputedAmount("100");
        request.setReason("Incorrect pricing");
        return request;
    }
}
