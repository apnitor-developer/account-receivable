package com.example.account.receivable.Payment.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Sort;

import com.example.account.receivable.BankReconciliation.Enum.PaymentStatus;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Repository.CustomerRepository;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Enum.InvoiceStatus;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;
import com.example.account.receivable.Payment.MonthlyPaymentProjection;
import com.example.account.receivable.Payment.PaymentMethodReportProjection;
import com.example.account.receivable.Payment.Dto.ReceivePaymentRequest;
import com.example.account.receivable.Payment.Dto.ResponseDTO.ManualPaymentResponseDto;
import com.example.account.receivable.Payment.Dto.ResponseDTO.MonthlyPaymentDto;
import com.example.account.receivable.Payment.Dto.ResponseDTO.PaymentReportDto;
import com.example.account.receivable.Payment.Entity.Payment;
import com.example.account.receivable.Payment.Entity.PaymentApplication;
import com.example.account.receivable.Payment.Enum.PaymentMethod;
import com.example.account.receivable.Payment.Enum.PaymentSource;
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


    //Create Payment
    @Transactional
    public ManualPaymentResponseDto createManualPayment(
            Long customerId,
            ReceivePaymentRequest request
    ) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        Payment payment = Payment.builder()
                .customer(customer)
                .bankDeposit(request.getBankDeposit())
                .serviceFee(request.getServiceFee())
                .paymentAmount(request.getPaymentAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentDate(LocalDate.now())
                .source(PaymentSource.MANUAL)
                .status(PaymentStatus.CREATED)
                .notes(request.getNotes())
                .build();

        payment = paymentRepository.save(payment);

        return mapToManualPaymentResponse(payment);
    }


    // Mapper method for Manual Payment Response
    private ManualPaymentResponseDto mapToManualPaymentResponse(Payment payment) {

        ManualPaymentResponseDto dto = new ManualPaymentResponseDto();

        dto.setPaymentId(payment.getId());
        dto.setBankDeposit(payment.getBankDeposit());
        dto.setServiceFee(payment.getServiceFee());
        dto.setPaymentAmount(payment.getPaymentAmount());

        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setSource(payment.getSource());
        dto.setStatus(payment.getStatus());

        dto.setPaymentDate(payment.getPaymentDate());
        dto.setNotes(payment.getNotes());

        dto.setCustomerId(payment.getCustomer().getId());
        dto.setCustomerName(payment.getCustomer().getCustomerName());

        dto.setCreatedAt(payment.getCreatedAt());

        return dto;
    }


    // Approve payment and Apply on the Invoice
    @Transactional
    public Payment approveAndApplyPayment(Long paymentId, List<Long> invoiceIds) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

        if (payment.getStatus() != PaymentStatus.CREATED) {
            throw new IllegalStateException("Payment already processed");
        }

        // Apply invoices
        applyInvoices(payment, invoiceIds);

        // 2️⃣ Final status
        payment.setStatus(PaymentStatus.APPROVED);

        return paymentRepository.save(payment);
        
    }



    // Apply Payment on the Invoice Helper Method
    @Transactional
    public void applyInvoices(Payment payment, List<Long> invoiceIds) {

        BigDecimal remainingPayment = payment.getPaymentAmount();

        // Load selected invoices
        List<Invoice> invoices = invoiceRepository.findAllById(invoiceIds);

        for (Invoice invoice : invoices) {

            if (remainingPayment.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal invoiceBalance = invoice.getBalanceDue();
            BigDecimal appliedAmount = invoiceBalance.min(remainingPayment);

            // OPEN AMOUNT = invoice balance BEFORE applying payment
            BigDecimal openAmountBefore = invoiceBalance;

            // New balance after applying
            BigDecimal newBalance = invoiceBalance.subtract(appliedAmount);

            // Create PaymentApplication
            PaymentApplication pa = PaymentApplication.builder()
                    .payment(payment)
                    .invoice(invoice)
                    .appliedAmount(appliedAmount)
                    .openAmount(openAmountBefore)
                    .newBalance(newBalance)
                    .build();

            paymentApplicationRepository.save(pa);

            // Update invoice
            invoice.setBalanceDue(newBalance);

            if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
                invoice.setStatus(InvoiceStatus.PAID);
            } else {
                invoice.setStatus(InvoiceStatus.PARTIAL);
            }

            invoice.setLastPaymentDate(LocalDate.now());
            invoiceRepository.save(invoice);

            // Reduce remaining payment
            remainingPayment = remainingPayment.subtract(appliedAmount);
        }
    }


    //Get unmatched payments
    public Map<String, Object> getUnmatchedSummary(Long companyId) {

        // 1️⃣ Manual unmatched (PAYMENTS)
        BigDecimal manualUnmatched = paymentRepository
                .sumManualUnmatched(companyId); // we'll define this

        Long manualCount = paymentRepository
                .countManualUnmatched(companyId);

        // 2️⃣ BAI unmatched (BANK_TRANSACTION)
        BigDecimal baiUnmatched = paymentRepository
                .sumUnmatchedBankTransactions(companyId);

        Long baiCount = paymentRepository
                .countUnmatchedBankTransactions(companyId);

        BigDecimal totalAmount = manualUnmatched.add(baiUnmatched);
        Long totalCount = manualCount + baiCount;

        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", totalAmount);
        result.put("totalCount", totalCount);

        return result;
    }



    // Get only the DRAFT Payments
    public Page<ManualPaymentResponseDto> getCreatedPayments(
            Long companyId,
            int page,
            int size,
            LocalDate fromDate,
            LocalDate toDate,
            Integer months
    ) {
        Pageable pageable =
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        LocalDate resolvedFrom = fromDate;
        LocalDate resolvedTo = toDate;

        if (resolvedFrom == null && resolvedTo == null && months != null && months > 0) {
            resolvedTo = LocalDate.now();
            resolvedFrom = resolvedTo.minusMonths(months);
        }

        ZoneId zone = ZoneId.systemDefault();

        Instant fromInstant = resolvedFrom != null
                ? resolvedFrom.atStartOfDay(zone).toInstant()
                : null;

        Instant toInstant = resolvedTo != null
                ? resolvedTo.plusDays(1).atStartOfDay(zone).toInstant()
                : null;

        Page<Payment> payments =
                paymentRepository.findPaymentsByCompanyStatusAndDateRange(
                        companyId,
                        PaymentStatus.CREATED,
                        fromInstant,
                        toInstant,
                        pageable
                );

        return payments.map(this::mapToManualPaymentResponse);
    }




    // //Auto Apply Payment
    // @Transactional
    // public Payment applyPayment(Long customerId, ReceivePaymentRequest request) {

    //     // Validate customer
    //     Customer customer = customerRepository.findById(customerId)
    //             .orElseThrow(() -> new ResponseStatusException(
    //                     HttpStatus.NOT_FOUND, "Customer not found"));

    //     // Create payment record
    //     Payment payment = Payment.builder()
    //             .customer(customer)
    //             .bankDeposit(request.getBankDeposit())
    //             .serviceFee(request.getServiceFee())
    //             .paymentAmount(request.getPaymentAmount())
    //             .paymentMethod(request.getPaymentMethod())
    //             .paymentDate(LocalDate.now())
    //             .source(PaymentSource.MANUAL)
    //             .status(PaymentStatus.DRAFT)
    //             .notes(request.getNotes())
    //             .build();

    //     payment = paymentRepository.save(payment);


    //     // Resolve company from customer
    //     Company company =
    //         CompanyResolver.resolveCompanyForCustomer(customer);

    //     // SAVE TRANSACTION
    //     glTransactionService.createTransaction(
    //         company.getId(),
    //         GlTransactionCreateRequest.builder()
    //             .referenceType(GlReferenceType.PAYMENT)
    //             .referenceId(payment.getId())
    //             .referenceNumber("PAY-" + payment.getId())
    //             .amount(payment.getPaymentAmount())
    //             .transactionDate(payment.getPaymentDate())
    //             .description("Payment received")
    //             .build()
    //     );

    //     // Auto-apply logic
    //     BigDecimal remainingPayment = request.getPaymentAmount();

    //     // Load all selected invoices
    //     List<Invoice> invoices = invoiceRepository.findAllById(request.getInvoiceIds());

    //     for (Invoice invoice : invoices) {

    //         if (remainingPayment.compareTo(BigDecimal.ZERO) <= 0) break;

    //         BigDecimal invoiceBalance = invoice.getBalanceDue();
    //         BigDecimal appliedAmount = invoiceBalance.min(remainingPayment);

    //         // OPEN AMOUNT = invoiceBalance BEFORE applying payment
    //         BigDecimal openAmountBefore = invoiceBalance;

    //         //new balance
    //         BigDecimal balance = invoiceBalance.subtract(appliedAmount);

    //         PaymentApplication pa = PaymentApplication.builder()
    //                 .payment(payment)
    //                 .invoice(invoice)
    //                 .appliedAmount(appliedAmount)
    //                 .openAmount(openAmountBefore)    // ← storing in DB
    //                 .newBalance(invoiceBalance)
    //                 .build();

    //         paymentApplicationRepository.save(pa);

    //         // reduce invoice balance
    //         invoice.setBalanceDue(invoiceBalance.subtract(appliedAmount));

    //         // update status
    //         if (invoice.getBalanceDue().compareTo(BigDecimal.ZERO) == 0) {
    //             invoice.setStatus(InvoiceStatus.PAID);
    //         } else {
    //             invoice.setStatus(InvoiceStatus.PARTIAL);
    //         }

    //         invoice.setLastPaymentDate(LocalDate.now());
    //         invoiceRepository.save(invoice);

    //         // reduce remaining payment
    //         remainingPayment = remainingPayment.subtract(appliedAmount);
    //     }

    //     // Use the updatePromiseToPayStatus method to update the status of promise-to-pay 
    //     updatePromiseToPayStatus(customer, request.getPaymentAmount());

    //     return payment;
    // }


    // //Helper function use in the applyPayment method()
    // private void updatePromiseToPayStatus(Customer customer, BigDecimal paymentAmount) {

    //     // Remaining amount from this payment
    //     BigDecimal remainingPayment = paymentAmount;

    //     // Fetch active promises in ORDER (important!)
    //     List<PromiseToPay> activePromises =
    //             promiseToPayRepo.findByCustomerId(customer.getId())
    //                     .stream()
    //                     .filter(p ->
    //                             p.getStatus() == PromiseStatus.PENDING ||
    //                             p.getStatus() == PromiseStatus.DUE_TODAY
    //                     )
    //                     // optional: oldest promise first
    //                     .sorted(Comparator.comparing(PromiseToPay::getPromiseDate))
    //                     .toList();

    //     for (PromiseToPay promise : activePromises) {

    //         if (remainingPayment.compareTo(BigDecimal.ZERO) <= 0) {
    //             break; // no money left
    //         }

    //         BigDecimal promisedAmount = promise.getAmountPromised();

    //         if (remainingPayment.compareTo(promisedAmount) >= 0) {
    //             // fulfill this promise
    //             promise.setStatus(PromiseStatus.COMPLETED);
    //             promiseToPayRepo.save(promise);

    //             // deduct used amount
    //             remainingPayment = remainingPayment.subtract(promisedAmount);
    //         }
    //     }
    // }




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


    // Get All Payment 
    public Page<Payment> getPaymentsByCompanyId(
            Long companyId,
            int page,
            int size,
            LocalDate fromDate,
            LocalDate toDate,
            Integer months
    ) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "fromDate cannot be after toDate"
            );
        }

        LocalDate resolvedFrom = fromDate;
        LocalDate resolvedTo = toDate;

        // Apply months filter ONLY if explicit dates are not provided
        if (resolvedFrom == null && resolvedTo == null && months != null && months > 0) {
            resolvedTo = LocalDate.now();
            resolvedFrom = resolvedTo.minusMonths(months);
        }

        Pageable pageable =
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));

        return paymentRepository.findPaymentsByCompanyIdFilteredAndStatus(
                companyId,
                PaymentStatus.APPROVED,
                resolvedFrom,
                resolvedTo,
                pageable
        );
    }




    //Used to calculate the payment report(BANK_TRANSFER , CASH , UPI etc)
    public PaymentReportDto getPaymentReport(Long companyId, int months) {

        LocalDate fromDate = LocalDate.now().minusMonths(months);

        List<PaymentMethodReportProjection> rows =
                paymentRepository.getPaymentReport(companyId, fromDate);

        Map<PaymentMethod, Long> result = new LinkedHashMap<>();
        long total = 0;

        for (PaymentMethodReportProjection row : rows) {
            result.put(row.getPaymentMethod(), row.getCount());
            total += row.getCount();
        }

        return new PaymentReportDto(result, total);
    }



    //Method used to show the paymnet per months
    public List<MonthlyPaymentDto> getMonthlyPaymentsByYear(
        Long companyId,
        int year
    ) {
        List<MonthlyPaymentProjection> rows =
                paymentRepository.getMonthlyPaymentsByYear(companyId, year);

        Map<Integer, BigDecimal> monthMap = new HashMap<>();

        for (MonthlyPaymentProjection row : rows) {
            monthMap.put(row.getMonth(), row.getTotal());
        }

        List<MonthlyPaymentDto> result = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            BigDecimal amount = monthMap.getOrDefault(month, BigDecimal.ZERO);

            String monthName = Month.of(month)
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            result.add(new MonthlyPaymentDto(monthName, amount));
        }

        return result;
    }



    @Transactional
    public void applyExactAmount(
            Payment payment,
            Invoice invoice,
            BigDecimal amount
    ) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal invoiceBalance = invoice.getBalanceDue();

        if (invoiceBalance.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // Do not over-apply
        BigDecimal appliedAmount = amount.min(invoiceBalance);

        // Create PaymentApplication record
        PaymentApplication paymentApplication = PaymentApplication.builder()
                .payment(payment)
                .invoice(invoice)
                .appliedAmount(appliedAmount)
                .openAmount(invoiceBalance)
                .newBalance(invoiceBalance.subtract(appliedAmount))
                .build();

        paymentApplicationRepository.save(paymentApplication);

        // Update invoice balance
        BigDecimal newBalance = invoiceBalance.subtract(appliedAmount);
        invoice.setBalanceDue(newBalance);

        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIAL);
        }

        invoice.setLastPaymentDate(LocalDate.now());
    }


}
