package com.example.account.receivable.Payment.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Sort;

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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentApplicationRepository paymentApplicationRepository;
    private final PromiseToPayRepo promiseToPayRepo;

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

        // Use the updatePromiseToPayStatus method to update the status of promise-to-pay 
        updatePromiseToPayStatus(customer, request.getPaymentAmount());

        return payment;
    }


    //Helper function use in the applyPayment method()
    private void updatePromiseToPayStatus(Customer customer, BigDecimal paymentAmount) {

        // Remaining amount from this payment
        BigDecimal remainingPayment = paymentAmount;

        // Fetch active promises in ORDER (important!)
        List<PromiseToPay> activePromises =
                promiseToPayRepo.findByCustomerId(customer.getId())
                        .stream()
                        .filter(p ->
                                p.getStatus() == PromiseStatus.PENDING ||
                                p.getStatus() == PromiseStatus.DUE_TODAY
                        )
                        // optional: oldest promise first
                        .sorted(Comparator.comparing(PromiseToPay::getPromiseDate))
                        .toList();

        for (PromiseToPay promise : activePromises) {

            if (remainingPayment.compareTo(BigDecimal.ZERO) <= 0) {
                break; // no money left
            }

            BigDecimal promisedAmount = promise.getAmountPromised();

            if (remainingPayment.compareTo(promisedAmount) >= 0) {
                // fulfill this promise
                promise.setStatus(PromiseStatus.COMPLETED);
                promiseToPayRepo.save(promise);

                // deduct used amount
                remainingPayment = remainingPayment.subtract(promisedAmount);
            }
        }
    }




    //Get All Payments
    public Page<Payment> getAllPayments(int page , int size) {
        Pageable pageable = PageRequest.of(page, size , Sort.by("paymentDate").descending());
        return paymentRepository.findAll(pageable);
    }


    //Get Payments By the CompanyId
    public Page<Payment> getPaymentsByCompanyId(Long companyId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return paymentRepository.findPaymentsByCompanyId(companyId, pageable);
    }
}
