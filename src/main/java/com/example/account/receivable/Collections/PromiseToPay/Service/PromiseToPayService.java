package com.example.account.receivable.Collections.PromiseToPay.Service;

import org.springframework.stereotype.Service;

import com.example.account.receivable.Collections.PromiseToPay.DTO.RequestDTO.PromiseToPayRequest;
import com.example.account.receivable.Collections.PromiseToPay.DTO.ResponseDTO.PromiseToPayResponse;
import com.example.account.receivable.Collections.PromiseToPay.Entity.PromiseStatus;
import com.example.account.receivable.Collections.PromiseToPay.Entity.PromiseToPay;
import com.example.account.receivable.Collections.PromiseToPay.Repository.PromiseToPayRepo;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Repository.CustomerRepository;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromiseToPayService {

    private final PromiseToPayRepo promiseToPayRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;

    public PromiseToPayResponse createPromise(PromiseToPayRequest request) {

        System.out.println("Promise to pay Request" + request);

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Invoice invoice = null;
        if (request.getInvoiceId() != null) {
            invoice = invoiceRepository.findById(request.getInvoiceId())
                    .orElseThrow(() -> new RuntimeException("Invoice not found"));
        }

        PromiseToPay promise = PromiseToPay.builder()
                .customer(customer)
                .invoice(invoice)
                .amountPromised(request.getAmountPromised())
                .promiseDate(request.getPromiseDate())
                .notes(request.getNotes())
                .status(PromiseStatus.PENDING)
                .build();

        PromiseToPay saved = promiseToPayRepository.save(promise);

        return new PromiseToPayResponse(
                saved.getId(),
                customer.getCustomerName(),
                invoice != null ? invoice.getInvoiceNumber() : null,
                saved.getAmountPromised(),
                saved.getPromiseDate(),
                saved.getStatus(),
                saved.getNotes()
        );
    }
}
