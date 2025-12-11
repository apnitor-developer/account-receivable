package com.example.account.receivable.Payment.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Sort;


import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Repository.CustomerRepository;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;
import com.example.account.receivable.Payment.Dto.ReceivePaymentRequest;
import com.example.account.receivable.Payment.Entity.Payment;
import com.example.account.receivable.Payment.Entity.PaymentApplication;
import com.example.account.receivable.Payment.Repository.PaymentApplicationRepository;
import com.example.account.receivable.Payment.Repository.PaymentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentApplicationRepository paymentApplicationRepository;


    // @Transactional
    // public Payment applyPayment(Long customerId , ReceivePaymentRequest request) {
    //     // Validate customer
    //     Customer customer = customerRepository.findById(customerId)
    //             .orElseThrow(() -> new ResponseStatusException(
    //                     HttpStatus.NOT_FOUND, "Customer not found"));

    //     // Create payment record
    //     Payment payment = Payment.builder()
    //             .customer(customer)
    //             .paymentAmount(request.getPaymentAmount())
    //             .paymentMethod(request.getPaymentMethod())
    //             .paymentDate(LocalDate.now())
    //             .notes(request.getNotes())
    //             .build();

    //     payment = paymentRepository.save(payment);

    //     // Apply payment to invoices
    //     for (InvoicePaymentDto dto : request.getInvoicePayments()) {

    //         Invoice invoice = invoiceRepository.findById(dto.getInvoiceId())
    //                 .orElseThrow(() -> new ResponseStatusException(
    //                         HttpStatus.NOT_FOUND, "Invoice not found"));

    //         BigDecimal appliedAmount = dto.getAmount();

    //         // Create application record
    //         PaymentApplication pa = PaymentApplication.builder()
    //                 .payment(payment)
    //                 .invoice(invoice)
    //                 .appliedAmount(appliedAmount)
    //                 .build();

    //         paymentApplicationRepository.save(pa);

    //         // Update invoice balance
    //         invoice.setBalanceDue(invoice.getBalanceDue().subtract(appliedAmount));

    //         // Update invoice status
    //         if (invoice.getBalanceDue().compareTo(BigDecimal.ZERO) == 0) {
    //             invoice.setStatus("PAID");
    //         } else {
    //             invoice.setStatus("PARTIAL");
    //         }

    //         invoice.setLastPaymentDate(LocalDate.now());
    //         invoiceRepository.save(invoice);
    //     }

    //     return payment;
    // }



    //Auto Apply Payment
    @Transactional
    public Payment applyPayment(Long customerId, ReceivePaymentRequest request) {

        // Validate customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found"));

        // Create payment record
        Payment payment = Payment.builder()
                .customer(customer)
                .paymentAmount(request.getPaymentAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentDate(LocalDate.now())
                .notes(request.getNotes())
                .build();

        payment = paymentRepository.save(payment);

        // Auto-apply logic
        BigDecimal remainingPayment = request.getPaymentAmount();

        // Load all selected invoices
        List<Invoice> invoices = invoiceRepository.findAllById(request.getInvoiceIds());

        for (Invoice invoice : invoices) {

            if (remainingPayment.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal invoiceBalance = invoice.getBalanceDue();
            BigDecimal appliedAmount = invoiceBalance.min(remainingPayment);

            // OPEN AMOUNT = invoiceBalance BEFORE applying payment
            BigDecimal openAmountBefore = invoiceBalance;

            //new balance
            BigDecimal balance = invoiceBalance.subtract(appliedAmount);
            System.out.println("newbalance" + balance);

            PaymentApplication pa = PaymentApplication.builder()
                    .payment(payment)
                    .invoice(invoice)
                    .appliedAmount(appliedAmount)
                    .openAmount(openAmountBefore)    // ← storing in DB
                    .newBalance(invoiceBalance)
                    .build();

            paymentApplicationRepository.save(pa);

            // reduce invoice balance
            invoice.setBalanceDue(invoiceBalance.subtract(appliedAmount));

            // update status
            if (invoice.getBalanceDue().compareTo(BigDecimal.ZERO) == 0) {
                invoice.setStatus("PAID");
            } else {
                invoice.setStatus("PARTIAL");
            }

            invoice.setLastPaymentDate(LocalDate.now());
            invoiceRepository.save(invoice);

            // reduce remaining payment
            remainingPayment = remainingPayment.subtract(appliedAmount);
        }

        return payment;
    }




    //Get All Payments
    public Page<Payment> getAllPayments(int page , int size) {
        Pageable pageable = PageRequest.of(page, size , Sort.by("paymentDate").descending());
        return paymentRepository.findAll(pageable);
    }
}
